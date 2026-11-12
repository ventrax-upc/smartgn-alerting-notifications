param([string]$BaseUrl = 'http://127.0.0.1:8086')
$ErrorActionPreference = 'Stop'
$health = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 10
if ($health.status -ne 'UP') { throw 'Alerting health is not UP.' }
$openapi = Invoke-RestMethod "$BaseUrl/v3/api-docs" -TimeoutSec 10
if (-not $openapi.paths.'/api/v1/notifications' -or -not $openapi.paths.'/internal/v1/telemetry-events') {
    throw 'Alerting OpenAPI is missing the notification or telemetry contract.'
}
Write-Output 'Alerting health and OpenAPI verified.'
