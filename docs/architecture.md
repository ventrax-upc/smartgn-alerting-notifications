# Arquitectura — SmartGN Alerting & Notifications

## 1. Contexto

El servicio consume eventos normalizados de telemetría y eventos operativos, evalúa reglas, persiste alertas y encola notificaciones. Un dispatcher independiente entrega por proveedores externos y registra el resultado.

```mermaid
flowchart LR
    T[Telemetry & Data Ingestion] -->|POST /internal/v1/telemetry-events| A[Alerting & Notifications]
    P[Payments & Billing] -->|POST /internal/v1/operational-events| A
    S[Support & Maintenance] -->|POST /internal/v1/operational-events| A
    I[Installation & Device Management] -->|POST /internal/v1/operational-events| A
    A -->|email| SG[SendGrid]
    A -->|SMS| TW[Twilio]
    A -->|WhatsApp| IB[Infobip]
    A -->|acuses| R[POST /internal/v1/notification-receipts]
    F[Frontend / API Gateway] -->|REST + X-Account-Id / X-Role| A
    A --> DB[(PostgreSQL)]
```

## 2. Capas

```mermaid
flowchart TB
    subgraph Interfaces
        C[Controllers]
        RQ[Resources]
        AS[Assemblers]
    end
    subgraph Application
        CS[Command Services]
        QS[Query Services]
        OP[Outbound Ports]
        SEED[Default Rules Seeder]
    end
    subgraph Domain
        AG[Aggregates & Entities]
        VO[Value Objects]
        DS[Domain Services]
        CMD[Commands & Queries]
    end
    subgraph Infrastructure
        JPA[JPA Repositories & Converters]
        ADP[Provider Adapters]
        STR[Strategies & Circuit Breaker]
        SCH[Dispatcher & Distributed Lock]
        SEC[Security]
    end
    C --> CS
    C --> QS
    C --> AS
    CS --> AG
    QS --> JPA
    CS --> JPA
    CS --> OP
    OP --> STR
    STR --> ADP
    SCH --> QS
    SCH --> CS
    SEC --> C
```

## 3. Modelo de dominio

```mermaid
classDiagram
    class Alert {
        Long id
        EventId eventId
        DeviceId deviceId
        PropertyId propertyId
        SupplyPointId supplyPointId
        AccountId accountId
        Long ruleId
        int ruleVersion
        RuleMetric metric
        RuleOperator operator
        BigDecimal threshold
        BigDecimal measuredValue
        String condition
        AlertSeverity severity
        AlertStatus status
        String instructions
        Instant measuredAt
        Instant receivedAt
        acknowledge()
        resolve()
    }

    class AlertRule {
        Long id
        String name
        RuleMetric metric
        RuleOperator operator
        Threshold threshold
        AlertSeverity severity
        boolean active
        int version
        matches(TelemetryReading)
        describeCondition(BigDecimal)
    }

    class Notification {
        Long id
        Long alertId
        EventId sourceEventId
        AccountId accountId
        NotificationType notificationType
        OperationalEventType operationalEventType
        NotificationChannel channel
        Recipient recipient
        DeliveryStatus status
        int attempts
        Instant nextAttemptAt
        markAttemptStarted()
        recordAccepted()
        recordTransientFailure()
        recordPermanentFailure()
        recordIndeterminate()
        markCircuitOpen()
    }

    class NotificationPreference {
        Long id
        AccountId accountId
        NotificationType notificationType
        NotificationChannel channel
        Recipient recipient
        boolean active
    }

    class NotificationAttempt {
        Long id
        Long notificationId
        int attemptNumber
        DeliveryStatus status
        String providerMessageId
        String errorCode
        String errorMessage
        Instant attemptedAt
    }

    Alert --> AlertRule : snapshots rule
    Notification --> Alert : alertId
    NotificationAttempt --> Notification : notificationId
    NotificationPreference --> Notification : configures
```

## 4. Evaluación de telemetría

```mermaid
sequenceDiagram
    participant T as Telemetry
    participant C as InternalTelemetryEventsController
    participant S as AlertCommandServiceImpl
    participant R as AlertRuleRepository
    participant A as AlertRepository
    participant P as NotificationPreferenceRepository
    participant N as NotificationRepository

    T->>C: POST /internal/v1/telemetry-events
    C->>S: EvaluateTelemetryCommand
    S->>R: find active rules
    loop each matching rule
        S->>A: existsBy(deviceId, eventId, ruleId)?
        alt duplicate
            S-->>S: skip
        else new
            S->>A: saveAndFlush(Alert)
            S->>P: find CRITICAL_ALERT preference
            alt preference exists
                S->>N: save(Notification PENDING)
            else no preference
                S-->>S: alert without notification
            end
        end
    end
    S-->>C: List<Alert>
    C-->>T: 200 {alertsCreated, alertIds}
```

La alerta y la notificación se guardan en la misma transacción. La transacción se cierra antes de que el dispatcher intente la entrega.

## 5. Despacho, reintentos y circuit breaker

```mermaid
sequenceDiagram
    participant Sch as NotificationDispatchScheduler
    participant L as DistributedDispatchLock
    participant Q as NotificationQueryService
    participant S as NotificationCommandServiceImpl
    participant B as ProviderCircuitBreaker
    participant St as NotificationStrategy
    participant Ad as NotificationAdapter
    participant P as Provider

    Sch->>L: tryAcquire()
    alt lock not acquired
        Sch-->>Sch: skip cycle
    else lock acquired
        Sch->>Q: find pending due
        loop each notification
            Sch->>S: DispatchNotificationCommand
            S->>B: allow(provider)?
            alt circuit open
                S->>S: markCircuitOpen(now + 30s)
            else allowed
                S->>St: send(message)
                St->>Ad: send(message)
                Ad->>P: HTTP POST (timeout 2s)
                P-->>Ad: accepted / error
                Ad-->>St: NotificationDispatchResult
                St->>B: recordSuccess / recordFailure
                S->>S: update status + attempt
            end
        end
    end
```

Estados de entrega:

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> ACCEPTED: proveedor acepta
    ACCEPTED --> DELIVERED: acuse de entrega
    PENDING --> PENDING: fallo transitorio / circuit open
    PENDING --> FAILED: fallo permanente
    PENDING --> INDETERMINATE: timeout / resultado desconocido
    ACCEPTED --> FAILED: acuse de fallo
    ACCEPTED --> INDETERMINATE: acuse indeterminado
```

## 6. Persistencia

| Tabla | Propósito | Restricciones clave |
| --- | --- | --- |
| `alert_rules` | Reglas de evaluación versionadas | `name` único |
| `alerts` | Snapshot de la alerta | `(device_id, event_id, rule_id)` único |
| `notification_preferences` | Canal y destinatario por cuenta/tipo | `(account_id, notification_type)` único |
| `notifications` | Outbox y estado de entrega | `(alert_id, channel, recipient)` y `(source_event_id, channel, recipient)` únicos |
| `notification_attempts` | Bitácora append-only de intentos/acuses | `(notification_id, attempt_number)` único |

El nombramiento físico es `snake_case` mediante `SnakeCasePhysicalNamingStrategy`. La auditoría usa `@CreatedDate` y `@LastModifiedDate`.

## 7. Seguridad

```mermaid
flowchart LR
    G[API Gateway] -->|X-Account-Id, X-Role| F[ActorAuthenticationFilter]
    F -->|ROLE_USER / ROLE_SUPERADMIN| C[Controllers]
    I[Internal service] -->|Bearer INTERNAL_SERVICE_TOKEN| F
    F -->|SYSTEM| IC[Internal controllers]
```

- `smartgn.security.enabled=false` por defecto para desarrollo local.
- Con seguridad activa, las rutas `/api/v1/**` requieren `X-Account-Id`; `SUPERADMIN` puede consultar todas las cuentas.
- Las rutas `/internal/**` requieren `Authorization: Bearer <INTERNAL_SERVICE_TOKEN>`.
- El gateway debe eliminar las cabeceras de identidad enviadas por el cliente.
- No se valida JWT dentro del servicio; la confianza se delega al gateway.

## 8. Decisiones y límites

- El circuit breaker es en memoria y por instancia. Con varias réplicas cada una mantiene su propio estado; el lock distribuido evita despachos simultáneos, pero no comparte el estado del circuito.
- El dispatcher realiza la llamada HTTP dentro de la transacción de actualización. Para volúmenes altos se recomienda separar “claim” y “send” en transacciones distintas.
- SmartGN no opera válvulas remotamente. `VALVE_STATE` solo genera una alerta informativa.
- Las reglas por defecto son datos de arranque, no migraciones; se pueden desactivar con `smartgn.alerting.seed-default-rules=false`.
