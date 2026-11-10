# Contratos REST e integración

Base local: `http://localhost:8084`.

El documento OpenAPI generado desde la aplicación está en [`docs/openapi.json`](openapi.json). También se expone en `/v3/api-docs` y `/swagger-ui.html`.

## 1. Convenciones

### Identidad

Con `smartgn.security.enabled=true`, las rutas públicas requieren:

```http
X-Account-Id: 11111111-1111-1111-1111-111111111111
X-Role: USER
```

`X-Role` admite `USER` o `SUPERADMIN`. Si se omite, se asume `USER`.

Las rutas internas requieren:

```http
Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
```

### Paginación

`GET /api/v1/alerts` y `GET /api/v1/notifications` aceptan `page` (base 0) y `size`. La respuesta incluye:

```http
X-Total-Count: 42
X-Page: 0
X-Page-Size: 20
```

### Errores

Los errores de validación y de negocio usan Problem Details (`application/problem+json`). El campo `detail` describe el problema y se expone también como `details` para clientes que usan ese alias. Códigos relevantes:

| Código | Situación |
| --- | --- |
| 400 | Validación, métrica faltante, umbral/operador inválido, destinatario inválido. |
| 401 | Falta identidad o token interno inválido. |
| 403 | La cuenta no coincide con el recurso o se requiere SUPERADMIN. |
| 404 | Alerta, regla o notificación inexistente. |
| 409 | Duplicado, transición de estado inválida, conflicto de integridad. |
| 422 | No existe preferencia activa para el aviso operativo. |

## 2. Alertas

### `GET /api/v1/alerts`

Parámetros opcionales: `supplyPointId`, `status`, `severity`, `from`, `to`, `page`, `size`.

`from` y `to` son ISO-8601 (`2026-11-10T12:00:00Z`).

Respuesta `200`:

```json
[
  {
    "id": 15,
    "eventId": "evt-2026-0001",
    "deviceId": "dev-001",
    "propertyId": "22222222-2222-2222-2222-222222222222",
    "supplyPointId": "33333333-3333-3333-3333-333333333333",
    "accountId": "11111111-1111-1111-1111-111111111111",
    "ruleId": 2,
    "ruleVersion": 1,
    "ruleName": "Presión crítica baja",
    "metric": "PRESSURE",
    "operator": "LESS_THAN",
    "threshold": 0.5000,
    "measuredValue": 0.4000,
    "condition": "Presión menor que 0.5000 kPa (valor medido: 0.4000 kPa)",
    "severity": "CRITICAL",
    "severityLabel": "Crítica",
    "status": "OPEN",
    "statusLabel": "Abierta",
    "instructions": "1. No manipule la válvula...",
    "measuredAt": "2026-11-10T12:00:00Z",
    "receivedAt": "2026-11-10T12:00:02Z",
    "createdAt": "2026-11-10T12:00:02Z",
    "acknowledgedAt": null,
    "resolvedAt": null
  }
]
```

### `GET /api/v1/alerts/{alertId}`

`200` con el objeto anterior; `404` si no existe; `403` si pertenece a otra cuenta.

### `POST /api/v1/alerts/{alertId}/acknowledge`

Solo desde `OPEN`. Respuesta `200` con la alerta actualizada; `409` si el estado no lo permite.

### `POST /api/v1/alerts/{alertId}/resolve`

Permitido desde `OPEN` o `ACKNOWLEDGED`. Respuesta `200`; `409` si ya está `RESOLVED`.

### `GET /api/v1/alerts/{alertId}/notifications`

`200` con la lista de notificaciones de la alerta.

## 3. Reglas

### `GET /api/v1/alert-rules?active=true`

`200`:

```json
[
  {
    "id": 2,
    "name": "Presión crítica baja",
    "metric": "PRESSURE",
    "metricLabel": "Presión",
    "operator": "LESS_THAN",
    "operatorLabel": "menor que",
    "threshold": 0.5000,
    "severity": "CRITICAL",
    "severityLabel": "Crítica",
    "description": "Presión por debajo del umbral crítico de seguridad.",
    "active": true,
    "version": 1,
    "createdAt": "2026-11-10T12:00:00Z",
    "updatedAt": "2026-11-10T12:00:00Z"
  }
]
```

### `POST /api/v1/alert-rules`

Requiere `SUPERADMIN`.

```json
{
  "name": "Presión crítica baja",
  "metric": "PRESSURE",
  "operator": "LESS_THAN",
  "threshold": 0.5,
  "severity": "CRITICAL",
  "description": "Presión por debajo del umbral crítico de seguridad.",
  "active": true
}
```

Respuesta `201` con la regla. `409` si el nombre ya existe. `400` si la combinación métrica/operador/umbral es inválida.

Reglas de validación:

- `GAS_PRESENCE` y `VALVE_STATE` solo admiten `IS_TRUE` o `IS_FALSE`, sin umbral.
- `PRESSURE` y `CONSUMPTION` admiten operadores numéricos y requieren umbral.
- El umbral admite hasta 4 decimales y 18 dígitos.

### `PUT/PATCH /api/v1/alert-rules/{ruleId}`

Mismo cuerpo. Acepta `PUT` o `PATCH` con el recurso completo. Incrementa `version` y devuelve `200`.

## 4. Preferencias

### `GET /api/v1/notification-preferences?accountId={uuid}`

`accountId` es opcional cuando la petición ya tiene identidad de cuenta; en ese caso se usa la cuenta del actor. Si la seguridad está desactivada, envía `accountId` explícito.

`200`:

```json
[
  {
    "id": 1,
    "accountId": "11111111-1111-1111-1111-111111111111",
    "notificationType": "CRITICAL_ALERT",
    "channel": "EMAIL",
    "channelLabel": "Correo electrónico",
    "recipient": "ops@smartgn.pe",
    "active": true,
    "createdAt": "2026-11-10T12:00:00Z",
    "updatedAt": "2026-11-10T12:00:00Z"
  }
]
```

### `PUT /api/v1/notification-preferences`

Upsert por `(accountId, notificationType)`.

```json
{
  "accountId": "11111111-1111-1111-1111-111111111111",
  "notificationType": "CRITICAL_ALERT",
  "channel": "EMAIL",
  "recipient": "ops@smartgn.pe",
  "active": true
}
```

Respuesta `200`. El destinatario se valida según el canal:

- `EMAIL`: formato de correo.
- `SMS` y `WHATSAPP`: `+` opcional y entre 7 y 15 dígitos.

## 5. Notificaciones

### `GET /api/v1/notifications`

Parámetros opcionales: `alertId`, `status`, `page`, `size`.

```json
[
  {
    "id": 8,
    "alertId": 15,
    "sourceEventId": null,
    "accountId": "11111111-1111-1111-1111-111111111111",
    "notificationType": "CRITICAL_ALERT",
    "operationalEventType": null,
    "channel": "EMAIL",
    "recipient": "ops@smartgn.pe",
    "subject": "[Crítica] Presión crítica baja - 33333333-3333-3333-3333-333333333333",
    "body": "Alerta SmartGN...",
    "status": "PENDING",
    "statusLabel": "Pendiente",
    "attempts": 0,
    "providerMessageId": null,
    "lastError": null,
    "nextAttemptAt": "2026-11-10T12:00:02Z",
    "createdAt": "2026-11-10T12:00:02Z",
    "updatedAt": "2026-11-10T12:00:02Z"
  }
]
```

### `GET /api/v1/notifications/{notificationId}/attempts`

`200`:

```json
[
  {
    "id": 1,
    "attemptNumber": 1,
    "status": "ACCEPTED",
    "providerMessageId": "sim-123",
    "errorCode": null,
    "errorMessage": null,
    "attemptedAt": "2026-11-10T12:00:03Z"
  }
]
```

## 6. Integración interna

### `POST /internal/v1/telemetry-events`

Evento normalizado por Telemetry & Data Ingestion. Al menos una métrica debe estar presente.

```json
{
  "eventId": "evt-2026-0001",
  "deviceId": "dev-001",
  "propertyId": "22222222-2222-2222-2222-222222222222",
  "supplyPointId": "33333333-3333-3333-3333-333333333333",
  "accountId": "11111111-1111-1111-1111-111111111111",
  "measuredAt": "2026-11-10T12:00:00Z",
  "receivedAt": "2026-11-10T12:00:02Z",
  "consumptionM3": 12.5,
  "pressureKpa": 0.4,
  "gasPresence": false,
  "valveOpen": false,
  "recipientEmail": "ops@smartgn.pe",
  "recipientPhone": "+51999999999"
}
```

Respuesta `200`:

```json
{
  "alertsCreated": 1,
  "alertIds": [15]
}
```

Idempotencia: si ya existe una alerta para `(deviceId, eventId, ruleId)`, la regla se omite y no se crea otra alerta ni otra notificación.

### `POST /internal/v1/operational-events`

Avisos de asignación, cita, pago o factura (US51). Usa la preferencia `OPERATIONAL_NOTICE` de la cuenta.

```json
{
  "eventId": "op-2026-0042",
  "accountId": "11111111-1111-1111-1111-111111111111",
  "eventType": "PAYMENT_CONFIRMED",
  "subject": "Pago confirmado",
  "body": "Su pago fue registrado correctamente."
}
```

Respuesta `201` con la lista de notificaciones creadas. `422` si no hay preferencia activa. `409` si ya existe la solicitud para el mismo evento, canal y destinatario.

### `POST /internal/v1/notification-receipts`

Acuse asíncrono del proveedor.

```json
{
  "providerMessageId": "sim-123",
  "status": "DELIVERED",
  "errorCode": null,
  "errorMessage": null
}
```

Respuesta `200` con la notificación actualizada. `404` si el `providerMessageId` no existe. `400` si el estado es `PENDING`.

## 7. Enumeraciones

| Enum | Valores |
| --- | --- |
| `RuleMetric` | `CONSUMPTION`, `PRESSURE`, `GAS_PRESENCE`, `VALVE_STATE` |
| `RuleOperator` | `GREATER_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `EQUALS`, `NOT_EQUALS`, `IS_TRUE`, `IS_FALSE` |
| `AlertSeverity` | `INFO`, `WARNING`, `CRITICAL` |
| `AlertStatus` | `OPEN`, `ACKNOWLEDGED`, `RESOLVED` |
| `NotificationType` | `CRITICAL_ALERT`, `OPERATIONAL_NOTICE` |
| `NotificationChannel` | `EMAIL`, `SMS`, `WHATSAPP` |
| `DeliveryStatus` | `PENDING`, `ACCEPTED`, `DELIVERED`, `FAILED`, `INDETERMINATE` |
| `OperationalEventType` | `ASSIGNMENT_CREATED`, `APPOINTMENT_CHANGED`, `PAYMENT_CONFIRMED`, `INVOICE_ISSUED`, `INSTALLATION_STATUS_CHANGED` |

## 8. Resultados de proveedor

| Resultado | Estado | Reintento |
| --- | --- | --- |
| Aceptado | `ACCEPTED` | No |
| Entregado | `DELIVERED` | No |
| Fallo transitorio | `PENDING` | 1 s, 2 s, 4 s y luego 30 s |
| Fallo permanente | `FAILED` | No |
| Indeterminado | `INDETERMINATE` | No |
| Circuito abierto | `PENDING` | 30 s |
