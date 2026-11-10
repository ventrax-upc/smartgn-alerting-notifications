# SmartGN Alerting & Notifications

Microservicio responsable de evaluar telemetría, generar alertas críticas, exponer instrucciones de seguridad, administrar preferencias de notificación y despachar mensajes por canales externos con patrón outbox, reintentos y circuit breaker.

Forma parte de la plataforma SmartGN/Ventrax y sigue la arquitectura y convenciones de `catch-up-platform` y de los servicios `smartgn-iam`, `smartgn-installation-device-management`, `smartgn-payments-billing` y `smartgn-support-maintenance`.

## Stack

- Java 25
- Spring Boot 4.1.2
- Spring Web MVC, Spring Data JPA, Bean Validation, Spring Security, Actuator
- PostgreSQL 17
- springdoc OpenAPI 3.1.1
- Maven 3.9.16 (wrapper incluido)
- H2 para pruebas de integración

## Contexto delimitado

Paquete raíz: `com.ventrax.alerting`.

Bounded context único: `notifications`.

| Capa | Paquete | Responsabilidad |
| --- | --- | --- |
| Domain | `notifications.domain.model` | Agregados `Alert`, `AlertRule`, `Notification`, `NotificationPreference`; entidad `NotificationAttempt`; value objects; comandos, queries y servicios de dominio. |
| Application | `notifications.application` | Contratos de command/query services, puertos de salida, implementaciones transaccionales y seeder de reglas. |
| Infrastructure | `notifications.infrastructure` | Repositorios JPA, convertidores, adaptadores SendGrid/Twilio/Infobip, simulador, circuit breaker, scheduler y lock distribuido. |
| Interfaces | `notifications.interfaces` | Controladores REST, resources y assemblers. |
| Shared | `alerting.shared` | `Result`, `PagedResult`, manejo global de errores, paginación, seguridad y Problem Details. |

## Funcionalidad

- Evaluación de eventos de telemetría contra reglas activas.
- Métricas soportadas: consumo, presión, presencia de gas y estado de válvula.
- Alertas con severidad, condición, valor medido, instrucciones de seguridad y ciclo de vida `OPEN → ACKNOWLEDGED → RESOLVED`.
- Deduplicación de alertas por `(deviceId, eventId, ruleId)`.
- Outbox transaccional: la alerta y su notificación pendiente se persisten en la misma transacción.
- Preferencias por cuenta y tipo de notificación (`CRITICAL_ALERT`, `OPERATIONAL_NOTICE`).
- Canales: email/SendGrid, SMS/Twilio y WhatsApp/Infobip.
- Modo `simulated` para desarrollo local sin llamadas externas.
- Reintentos: intento inicial y reintentos a 1 s, 2 s y 4 s; luego rechequeo cada 30 s.
- Circuit breaker por proveedor: 5 fallos consecutivos, apertura 30 s, sonda half-open.
- Timeout de proveedor configurable (2 s por defecto).
- Acuses asíncronos de proveedor y bitácora append-only de intentos.
- Lock distribuido con advisory lock de PostgreSQL para un solo dispatcher activo.
- Seguridad opcional basada en cabeceras confiables del gateway (`X-Account-Id`, `X-Role`) y token interno para `/internal/**`.

## API principal

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/v1/alerts` | Lista paginada de alertas. |
| GET | `/api/v1/alerts/{alertId}` | Detalle de alerta. |
| POST | `/api/v1/alerts/{alertId}/acknowledge` | Reconoce una alerta abierta. |
| POST | `/api/v1/alerts/{alertId}/resolve` | Resuelve una alerta. |
| GET | `/api/v1/alerts/{alertId}/notifications` | Notificaciones de una alerta. |
| GET | `/api/v1/alert-rules` | Lista reglas; filtro `active`. |
| GET | `/api/v1/alert-rules/{ruleId}` | Detalle de regla. |
| POST | `/api/v1/alert-rules` | Crea regla (SUPERADMIN). |
| PUT/PATCH | `/api/v1/alert-rules/{ruleId}` | Actualiza regla (SUPERADMIN). |
| GET | `/api/v1/notification-preferences?accountId=` | Preferencias de una cuenta; `accountId` es opcional si hay actor autenticado. |
| PUT | `/api/v1/notification-preferences` | Upsert de preferencia. |
| GET | `/api/v1/notifications` | Lista paginada de notificaciones. |
| GET | `/api/v1/notifications/{notificationId}` | Detalle de notificación. |
| GET | `/api/v1/notifications/{notificationId}/attempts` | Intentos/acuses de entrega. |
| POST | `/internal/v1/telemetry-events` | Evaluación de telemetría normalizada. |
| POST | `/internal/v1/operational-events` | Aviso operativo (US51). |
| POST | `/internal/v1/notification-receipts` | Acuse asíncrono de proveedor. |

La documentación interactiva está disponible en `/swagger-ui.html` y el documento OpenAPI en `/v3/api-docs`.

## Ejecución local

### Con Maven

Requisitos: JDK 25 y PostgreSQL en el puerto `5436` (o variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

```powershell
$env:JAVA_HOME='C:\Users\HP\.jdks\openjdk-26.0.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

El servicio escucha en `http://localhost:8084`.

### Con Docker Compose

```powershell
Copy-Item .env.example .env
# Edite .env y defina DB_PASSWORD e INTERNAL_SERVICE_TOKEN
docker compose up --build
```

PostgreSQL queda en `127.0.0.1:5436` y el servicio en `127.0.0.1:8084`.

### Modo simulado

Por defecto `smartgn.notifications.mode=simulated`. El adaptador simulado no realiza llamadas de red y devuelve resultados deterministas según el destinatario:

- contiene `retry` → fallo transitorio;
- contiene `fail` → fallo permanente;
- contiene `indeterminate` → resultado indeterminado;
- cualquier otro → aceptado con id `sim-<uuid>`.

Para usar proveedores reales:

```yaml
smartgn:
  notifications:
    mode: live
```

y configure `SENDGRID_API_KEY`, `TWILIO_*` e `INFOBIP_*`.

## Pruebas

```powershell
$env:JAVA_HOME='C:\Users\HP\.jdks\openjdk-26.0.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B test
```

La suite usa H2 en modo PostgreSQL y desactiva el scheduler y el seeder. Cubre:

- reglas numéricas y booleanas;
- política de reintentos;
- circuit breaker;
- flujo completo telemetría → alerta → outbox.

## Decisiones de diseño

- **Outbox transaccional**: `AlertCommandServiceImpl` guarda la alerta y la notificación pendiente dentro de la misma transacción. El dispatcher es independiente.
- **Deduplicación**: la unicidad de alerta se refuerza con una restricción de base de datos; la de notificaciones por alerta/canal/destinatario y por evento/canal/destinatario.
- **Strategy + Adapter**: `NotificationStrategy` resuelve el canal; `NotificationAdapter` encapsula el proveedor.
- **Circuit breaker**: se cuenta por proveedor (`SENDGRID`, `TWILIO`, `INFOBIP`). Los fallos permanentes no abren el circuito porque son específicos de la solicitud.
- **Reintentos**: los fallos transitorios vuelven a `PENDING` con `nextAttemptAt`; los permanentes pasan a `FAILED`; los indeterminados pasan a `INDETERMINATE`.
- **Seguridad**: el gateway valida el JWT y reenvía identidad; el servicio no valida JWT localmente. En local la seguridad está desactivada por defecto.
- **Válvulas**: SmartGN nunca opera válvulas remotamente. Las instrucciones son informativas y se almacenan con la alerta.

## Documentación

- [Arquitectura](docs/architecture.md)
- [Contratos REST e integración](docs/contracts.md)
- [Desarrollo local](docs/local-development.md)
- [Trazabilidad de requisitos](docs/service-overview.md)
- [OpenAPI generado](docs/openapi.json)
- [Integración con el frontend](docs/frontend-integration.md)
