# Service overview — Alerting & Notifications

## 1. Propósito

Centralizar las alertas críticas de SmartGN, las instrucciones de seguridad asociadas y los avisos operativos, desacoplando la evaluación de telemetría del envío por canales externos.

## 2. Alcance

Incluye:

- motor de reglas sobre telemetría normalizada;
- alertas con severidad, condición, valor medido e instrucciones;
- ciclo de vida de la alerta;
- preferencias de notificación por cuenta y tipo;
- outbox transaccional;
- dispatcher con reintentos y circuit breaker;
- adaptadores SendGrid, Twilio e Infobip;
- acuses de proveedor y bitácora de intentos;
- consultas paginadas para el frontend y administración de reglas.

No incluye:

- ingesta MQTT o normalización de telemetría (pertenece a Telemetry & Data Ingestion);
- operación remota de válvulas;
- gestión de identidades o emisión de JWT (pertenece a IAM);
- facturación, pagos o soporte;
- plantillas editables por el usuario final.

## 3. Trazabilidad

### US20 — Alertas críticas

| Requisito | Implementación |
| --- | --- |
| Detectar condiciones críticas de gas, presión, consumo y válvula. | `RuleMetric`, `RuleOperator`, `AlertRule.matches`. |
| Crear alerta con severidad y contexto. | Agregado `Alert` con snapshot de regla, inmueble, punto de suministro, dispositivo y valor medido. |
| Evitar duplicados por evento/dispositivo/regla. | Restricción única `(device_id, event_id, rule_id)` y verificación previa. |
| Persistir alerta y solicitud de notificación de forma atómica. | `AlertCommandServiceImpl` dentro de una transacción; outbox `Notification`. |
| Consultar y reconocer alertas. | `GET /api/v1/alerts`, `POST /api/v1/alerts/{id}/acknowledge`, `POST .../resolve`. |

### US22 — Instrucciones de seguridad

| Requisito | Implementación |
| --- | --- |
| Mostrar instrucciones claras y contextualizadas. | `AlertGuidanceCatalog` genera instrucciones según métrica y severidad; se guardan en `Alert.instructions`. |
| No ejecutar acciones remotas sobre válvulas. | No existe comando de operación de válvula; `VALVE_STATE` solo genera una alerta informativa. |
| Mantener las instrucciones disponibles en la consulta. | `AlertResource.instructions`. |

### US51 — Avisos operativos

| Requisito | Implementación |
| --- | --- |
| Notificar asignaciones, citas, pagos, boletas y cambios de instalación. | `OperationalEventType` y `POST /internal/v1/operational-events`. |
| Respetar el canal y destinatario configurado. | `NotificationPreference` tipo `OPERATIONAL_NOTICE`. |
| Evitar avisos duplicados. | Unicidad `(source_event_id, channel, recipient)` y verificación previa. |
| Responder si no hay destinatario. | `422` con `notification.error.noRecipient`. |

### QAS9 — Canales externos

| Requisito | Implementación |
| --- | --- |
| Email, SMS y WhatsApp. | `NotificationChannel`, estrategias y adaptadores SendGrid/Twilio/Infobip. |
| Reintentos ante fallos transitorios. | `NotificationRetryPolicy`: 1 s, 2 s, 4 s; luego 30 s. |
| Circuit breaker. | `ProviderCircuitBreaker`: 5 fallos, 30 s abierto, sonda half-open. |
| Timeout. | `ProviderHttpClientFactory`: 2 s configurable. |
| Resultado indeterminado. | `DeliveryStatus.INDETERMINATE` para timeouts o respuestas ambiguas. |
| Acuses asíncronos. | `POST /internal/v1/notification-receipts` y `NotificationAttempt`. |
| Operación local sin proveedores. | `SimulatedNotificationAdapter` con `smartgn.notifications.mode=simulated`. |

## 4. Actores

| Actor | Uso |
| --- | --- |
| Usuario de cuenta | Consulta alertas y notificaciones de su cuenta, reconoce y resuelve alertas, configura preferencias. |
| SUPERADMIN | Administra reglas y consulta cualquier cuenta. |
| Telemetry & Data Ingestion | Envía eventos normalizados. |
| Payments & Billing, Support & Maintenance, Installation & Device Management | Envían eventos operativos. |
| Proveedores externos | Reciben mensajes y envían acuses. |
| API Gateway | Valida JWT y reenvía identidad confiable. |

## 5. Indicadores operativos

- `alertsCreated` por evento evaluado.
- Notificaciones por estado (`PENDING`, `ACCEPTED`, `DELIVERED`, `FAILED`, `INDETERMINATE`).
- Intentos por notificación.
- Fallos por proveedor y estado del circuito (en logs; no persistido).
- Salud de aplicación y base de datos en `/actuator/health`.

## 6. Riesgos y mitigaciones

| Riesgo | Mitigación |
| --- | --- |
| Duplicados por reintentos del broker. | Deduplicación por restricción única y verificación previa. |
| Pérdida de notificaciones si el proveedor falla. | Outbox persistente, reintentos y rechequeo. |
| Caída prolongada del proveedor. | Circuit breaker y estado `PENDING` con reintento. |
| Timeout con resultado desconocido. | Estado `INDETERMINATE`; no se reintenta automáticamente para evitar duplicados. |
| Múltiples dispatchers. | Advisory lock de PostgreSQL. |
| Alertas sin destinatario. | La alerta se conserva; se registra advertencia y no se crea notificación. |
| Válvulas operadas por error. | El servicio es de solo lectura sobre telemetría; no existe actuador. |

## 7. Fuera de alcance y evolución

- Persistir el estado del circuit breaker para compartirlo entre réplicas.
- Separar el “claim” y el envío del dispatcher en transacciones distintas para mayor escala.
- Plantillas de mensaje administrables.
- Reintento manual desde el frontend.
- Historial de cambios de reglas (auditoría de versiones).
