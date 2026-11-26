$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$name = 'smartgn-alerting-verification-' + [Guid]::NewGuid().ToString('N')
$password = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))

docker run --rm -d --name $name -e "POSTGRES_PASSWORD=$password" -e POSTGRES_DB=verification postgres:18 | Out-Null
if ($LASTEXITCODE) { throw 'Could not start Alerting verification database.' }

try {
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        docker exec $name pg_isready -h 127.0.0.1 -U postgres -d verification *> $null
        if ($LASTEXITCODE -eq 0) {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'Alerting verification database did not become ready.' }

    docker run --rm --network "container:$name" `
        -e 'ALERTING_TEST_DB_URL=jdbc:postgresql://127.0.0.1:5432/verification?currentSchema=alerting' `
        -e "ALERTING_TEST_DB_PASSWORD=$password" `
        -v "${repo}:/workspace" -v "${repo}/.local/m2:/root/.m2" -w /workspace `
        maven:3.9.11-eclipse-temurin-25 mvn -B --no-transfer-progress verify
    if ($LASTEXITCODE) { throw 'Alerting verification failed.' }
} finally {
    docker stop $name *> $null
}
