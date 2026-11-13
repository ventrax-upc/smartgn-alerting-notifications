# Desarrollo local

## Requisitos

- JDK 25 (el wrapper compila con `release 25`).
- Docker Desktop con motor Linux (opcional, para PostgreSQL y compose).
- PowerShell 7 en Windows.

## 1. Compilar y probar

```powershell
$env:JAVA_HOME='C:\Users\HP\.jdks\openjdk-26.0.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B test
```

Las pruebas usan H2 en memoria con `MODE=PostgreSQL`, `ddl-auto=create-drop`, scheduler desactivado y seeder desactivado. No requieren PostgreSQL.

Para empaquetar:

```powershell
.\mvnw.cmd -B -DskipTests package
```

## 2. PostgreSQL local

Opción A: Docker Compose completo.

```powershell
Copy-Item .env.example .env
# Edite .env: DB_PASSWORD e INTERNAL_SERVICE_TOKEN
docker compose up --build
```

Opción B: solo PostgreSQL.

```powershell
docker run --name smartgn-alerting-db `
  -e POSTGRES_DB=smartgn_alerting_db `
  -e POSTGRES_USER=smartgn_alerting `
  -e POSTGRES_PASSWORD=smartgn_alerting `
  -p 127.0.0.1:5436:5432 `
  -d postgres:17.6
```

## 3. Ejecutar el servicio

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5436/smartgn_alerting_db'
$env:DB_USERNAME='smartgn_alerting'
$env:DB_PASSWORD='smartgn_alerting'
$env:SECURITY_ENABLED='false'
$env:NOTIFICATIONS_MODE='simulated'
.\mvnw.cmd spring-boot:run
```

- API: `http://localhost:8086`
- Swagger UI: `http://localhost:8086/swagger-ui.html`
- OpenAPI: `http://localhost:8086/v3/api-docs`
- Health: `http://localhost:8086/actuator/health`

## 4. Prueba manual del flujo

Con seguridad desactivada, cree una regla y una preferencia:

```powershell
$rule = @{
  name='Presión crítica baja'
  metric='PRESSURE'
  operator='LESS_THAN'
  threshold=0.5
  severity='CRITICAL'
  description='Prueba local'
  active=$true
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8086/api/v1/alert-rules `
  -ContentType 'application/json' -Body $rule

$pref = @{
  accountId='11111111-1111-1111-1111-111111111111'
  notificationType='CRITICAL_ALERT'
  channel='EMAIL'
  recipient='ops@smartgn.pe'
  active=$true
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri http://localhost:8086/api/v1/notification-preferences `
  -ContentType 'application/json' -Body $pref
```

Envíe telemetría:

```powershell
$event = @{
  eventId='evt-local-1'
  deviceId='dev-local-1'
  propertyId='22222222-2222-2222-2222-222222222222'
  supplyPointId='33333333-3333-3333-3333-333333333333'
  accountId='11111111-1111-1111-1111-111111111111'
  measuredAt=(Get-Date).ToUniversalTime().ToString('o')
  pressureKpa=0.4
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8086/internal/v1/telemetry-events `
  -ContentType 'application/json' -Body $event
```

Consulte el resultado:

```powershell
Invoke-RestMethod http://localhost:8086/api/v1/alerts
Invoke-RestMethod http://localhost:8086/api/v1/notifications
```

El scheduler despacha cada segundo. En modo simulado la notificación pasa a `ACCEPTED` con un id `sim-<uuid>`.

## 5. Seguridad local

Para probar la seguridad:

```powershell
$env:SECURITY_ENABLED='true'
$env:INTERNAL_SERVICE_TOKEN='local-internal-token'
```

Rutas públicas:

```powershell
$headers = @{
  'X-Account-Id'='11111111-1111-1111-1111-111111111111'
  'X-Role'='USER'
}
Invoke-RestMethod -Headers $headers http://localhost:8086/api/v1/alerts
```

Rutas internas:

```powershell
$internal = @{ Authorization='Bearer local-internal-token' }
Invoke-RestMethod -Headers $internal -Method Post `
  -Uri http://localhost:8086/internal/v1/telemetry-events `
  -ContentType 'application/json' -Body $event
```

## 6. Variables de entorno

| Variable | Default | Descripción |
| --- | --- | --- |
| `PORT` | `8086` | Puerto HTTP. |
| `DB_URL` | `jdbc:postgresql://localhost:5436/smartgn_alerting_db` | URL JDBC. |
| `DB_USERNAME` | `smartgn_alerting` | Usuario. |
| `DB_PASSWORD` | `smartgn_alerting` | Contraseña. |
| `SECURITY_ENABLED` | `false` | Activa la seguridad por cabeceras. |
| `INTERNAL_SERVICE_TOKEN` | valor de desarrollo | Token de `/internal/**`. |
| `ALLOWED_ORIGIN` | `http://localhost:4200` | Origen CORS. |
| `NOTIFICATIONS_MODE` | `simulated` | `simulated` o `live`. |
| `DISPATCH_ENABLED` | `true` | Activa el scheduler. |
| `DISPATCH_INTERVAL_MS` | `1000` | Intervalo del scheduler. |
| `DISPATCH_BATCH_SIZE` | `50` | Lote por ciclo. |
| `NOTIFICATION_TIMEOUT_SECONDS` | `2` | Timeout HTTP por proveedor. |
| `SEED_DEFAULT_RULES` | `true` | Carga reglas de ejemplo al arrancar. |
| `SENDGRID_API_KEY` | vacío | Credencial SendGrid. |
| `SENDGRID_FROM` | `no-reply@smartgn.pe` | Remitente email. |
| `TWILIO_ACCOUNT_SID` | vacío | Cuenta Twilio. |
| `TWILIO_AUTH_TOKEN` | vacío | Token Twilio. |
| `TWILIO_FROM` | vacío | Remitente SMS. |
| `INFOBIP_BASE_URL` | `https://api.infobip.com` | Base Infobip. |
| `INFOBIP_API_KEY` | vacío | Token Infobip. |
| `INFOBIP_WHATSAPP_FROM` | vacío | Remitente WhatsApp. |

## 7. Solución de problemas

- **`spring.jackson.serialization.write-dates-as-timestamps`**: no usar; Spring Boot 4 usa Jackson 3 y esa propiedad ya no existe.
- **`AutoConfigureMockMvc` no encontrado**: el proyecto incluye `spring-boot-starter-webmvc-test`; en Spring Boot 4 el paquete es `org.springframework.boot.webmvc.test.autoconfigure`.
- **El scheduler no despacha**: revise `DISPATCH_ENABLED`, que el lock distribuido se adquiera y que `next_attempt_at <= now()`.
- **`422` en aviso operativo**: cree una preferencia `OPERATIONAL_NOTICE` activa para la cuenta.
- **`409` al crear regla**: el nombre ya existe; use `PUT` o cambie el nombre.
- **`403` en consultas**: la cuenta del recurso no coincide con `X-Account-Id`; use `SUPERADMIN` solo para administración.
