param([switch]$Integrated, [switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
try {
    if ($Integrated) {
        & ../smartgn-api-gateway/scripts/start-integrated.ps1 -SkipBuild:$SkipBuild
        return
    }
    if (-not (Test-Path -LiteralPath .env)) {
        Copy-Item -LiteralPath .env.example -Destination .env
        throw 'Set DB_PASSWORD and INTERNAL_SERVICE_TOKEN in .env, then rerun this script.'
    }
    $composeArgs = @('compose', 'up', '-d', '--wait')
    if (-not $SkipBuild) { $composeArgs += '--build' }
    & docker @composeArgs
    if ($LASTEXITCODE -ne 0) { throw 'Alerting Compose startup failed.' }
    & ./scripts/smoke-local.ps1
} finally { Pop-Location }
