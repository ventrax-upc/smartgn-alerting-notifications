# Integración con smartgn-frontend

Este documento resume el contrato que debe consumir el frontend y los puntos que requieren una decisión de arquitectura. El backend escucha en `http://localhost:8084`.

## 1. Base URL y proxy local

El frontend usa un único `apiUrl`:

- `environment.development.ts`: `http://localhost:3000/api/v1` (fake API).
- `environment.ts`: `/api/v1` (producción).

Para integrar alertas sin romper los demás módulos, usa un proxy por path. Las rutas específicas deben ir antes del fallback `/api/v1`:

```json
{
  "/api/v1/alert-rules": { "target": "http://localhost:8084", "secure": false },
  "/api/v1/alerts": { "target": "http://localhost:8084", "secure": false },
  "/api/v1/notifications": { "target": "http://localhost:8084", "secure": false },
  "/api/v1/notification-preferences": { "target": "http://localhost:8084", "secure": false },
  "/api/v1": { "target": "http://localhost:3000", "secure": false }
}
```

En producción, configura un reverse proxy/gateway equivalente o cambia `apiUrl` al host del backend y ajusta `ALLOWED_ORIGIN`.

CORS por defecto: `http://localhost:4200`. Si usas otro puerto o dominio, define `ALLOWED_ORIGIN` con el origen exacto.

## 2. Autenticación

### Modo local / demo

`SECURITY_ENABLED=false` es el valor por defecto. No se requieren headers de identidad. El frontend puede llamar directamente.

### Modo producción

Hay dos caminos:

1. **Gateway (diseño actual del backend).** El gateway valida el JWT y reenvía:

   ```http
   X-Account-Id: <uuid de la cuenta principal>
   X-Role: USER | SUPERADMIN
   ```

   El gateway debe eliminar cualquier header de identidad enviado por el cliente. El backend no valida el JWT.

2. **Validación JWT en el backend (recomendado a medio plazo).** Validar el mismo `Authorization: Bearer` que usan los demás servicios y resolver la cuenta principal por IAM. Esto evita depender de un gateway solo para alertas.

El frontend actual solo envía `Authorization: Bearer`; no envía `X-Account-Id` ni `X-Role`. Por eso, con `SECURITY_ENABLED=true` y sin gateway, las llamadas darán 401.

## 3. Endpoints

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/v1/alerts` | Lista paginada. |
| GET | `/api/v1/alerts/{id}` | Detalle. |
| POST | `/api/v1/alerts/{id}/acknowledge` | Reconoce una alerta `OPEN`. |
| POST | `/api/v1/alerts/{id}/resolve` | Resuelve una alerta `OPEN` o `ACKNOWLEDGED`. |
| GET | `/api/v1/alerts/{id}/notifications` | Notificaciones de la alerta. |
| GET | `/api/v1/alert-rules` | Lista reglas; filtro `active`. |
| GET | `/api/v1/alert-rules/{id}` | Detalle de regla. |
| POST | `/api/v1/alert-rules` | Crea regla (`SUPERADMIN`). |
| PUT/PATCH | `/api/v1/alert-rules/{id}` | Actualiza regla (`SUPERADMIN`) con el body completo. |
| GET | `/api/v1/notification-preferences` | Preferencias; `accountId` opcional si hay actor. |
| PUT | `/api/v1/notification-preferences` | Upsert por `(accountId, notificationType)`. |
| GET | `/api/v1/notifications` | Lista paginada. |
| GET | `/api/v1/notifications/{id}` | Detalle. |
| GET | `/api/v1/notifications/{id}/attempts` | Intentos de entrega. |

Los endpoints `/internal/**` no son para el frontend.

## 4. Paginación

`GET /api/v1/alerts` y `GET /api/v1/notifications` aceptan `page` (base 0) y `size` (1..100; por defecto 20). Devuelven un array plano y estos headers:

```http
X-Total-Count: 42
X-Page: 0
X-Page-Size: 20
```

`BaseApiEndpoint.getAll()` no lee headers ni envía `page/size`. Para estas listas, implementa un método con `HttpParams` y `observe: 'response'`, o al menos envía `size=100` y pagina en cliente.

## 5. Errores

Todos los errores de negocio usan Problem Details (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Errors found: ...",
  "details": "Errors found: ...",
  "instance": "/api/v1/..."
}
```

`detail` y `details` contienen el mismo texto. Usa `detail` o `details` en el parser del frontend; el `BaseApiEndpoint.handleError` actual lee `details`/`message`, por lo que `details` mantiene compatibilidad.

Códigos relevantes:

| Código | Situación |
| --- | --- |
| 400 | Validación, métrica faltante, umbral/operador inválido, destinatario inválido. |
| 401 | Identidad faltante o token interno inválido. |
| 403 | La cuenta no coincide con el recurso o se requiere SUPERADMIN. |
| 404 | Alerta, regla, notificación o preferencia inexistente. |
| 409 | Duplicado, transición de estado inválida o conflicto de integridad. |
| 422 | No existe preferencia activa para el aviso operativo. |

## 6. Enums

- `AlertSeverity`: `INFO`, `WARNING`, `CRITICAL`.
- `AlertStatus`: `OPEN`, `ACKNOWLEDGED`, `RESOLVED`.
- `RuleMetric`: `CONSUMPTION`, `PRESSURE`, `GAS_PRESENCE`, `VALVE_STATE`.
- `RuleOperator`: `GREATER_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `EQUALS`, `NOT_EQUALS`, `IS_TRUE`, `IS_FALSE`.
- `NotificationChannel`: `EMAIL`, `SMS`, `WHATSAPP`.
- `NotificationType`: `CRITICAL_ALERT`, `OPERATIONAL_NOTICE`.
- `DeliveryStatus`: `PENDING`, `ACCEPTED`, `DELIVERED`, `FAILED`, `INDETERMINATE`.
- `OperationalEventType`: `ASSIGNMENT_CREATED`, `APPOINTMENT_CHANGED`, `PAYMENT_CONFIRMED`, `INVOICE_ISSUED`, `INSTALLATION_STATUS_CHANGED`.

Los labels (`severityLabel`, `statusLabel`, `metricLabel`, `operatorLabel`, `channelLabel`) están en español. La UI está en inglés; usa los códigos y traduce en Angular.

## 7. Reglas de formulario

- `GAS_PRESENCE` y `VALVE_STATE` solo admiten `IS_TRUE`/`IS_FALSE` y no llevan `threshold`.
- `PRESSURE` y `CONSUMPTION` requieren `threshold` y operadores numéricos.
- `UpdateAlertRuleResource` es un reemplazo completo: envía `name`, `metric`, `operator`, `severity`, `description`, `threshold` y `active`.
- Si `active` se omite en un update, el backend conserva el valor actual; no reactiva.
- No hay `DELETE` de reglas ni preferencias. Para desactivar, usa `PUT` en preferencias o `PUT`/`PATCH` en reglas con `active=false`.
- `GET /api/v1/notification-preferences` usa la cuenta del actor si se omite `accountId`. Con `SECURITY_ENABLED=false` no hay actor, así que envía `accountId` explícito.

## 8. Limitaciones conocidas y decisiones pendientes

- **Usuarios delegados:** el backend filtra por cuenta principal. El frontend no recibe `principalAccountId` en el sign-in de IAM. Para que un delegado vea alertas, el gateway debe enviar el ID de la cuenta principal o IAM debe exponerlo. Mientras tanto, usa una cuenta principal para pruebas.
- **Permisos por medidor:** el frontend maneja `VIEW_ALERTS` por medidor, pero el backend no valida ese permiso; solo filtra por cuenta y, opcionalmente, `supplyPointId`. Si se requiere seguridad por medidor, es una fase posterior.
- **IDs del fake API:** el fake API usa `usr-100`; el backend exige UUID para `accountId`. El IAM real usa UUID. Para pruebas locales contra el backend, usa una cuenta UUID o el IAM real.
- **Filtros:** alerts no tiene `propertyId`/`deviceId`/`accountId` (la cuenta se deduce del actor); notifications no tiene `channel`/`notificationType`. Se pueden filtrar en cliente o agregar después.
- **CORS:** si el frontend corre en `127.0.0.1:4200`, configura `ALLOWED_ORIGIN` con ese origen exacto.

## 9. Checklist para el frontend

- [ ] Configurar proxy por path o gateway.
- [ ] Definir si se usa `SECURITY_ENABLED=false` (demo) o gateway/JWT (producción).
- [ ] Implementar el módulo de alertas y el API service.
- [ ] Usar `PUT` o `PATCH` con body completo para reglas.
- [ ] Leer `details`/`detail` en errores.
- [ ] Implementar paginación con `HttpParams` y headers.
- [ ] Usar códigos de enum y traducir labels.
- [ ] Resolver nombres de propiedad/supply point/device con installation.
- [ ] Validar reglas booleanas vs numéricas en el formulario.
- [ ] Probar con una cuenta principal y, luego, definir el flujo para delegados.
