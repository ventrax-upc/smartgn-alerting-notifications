# SmartGN Alerting & Notifications

## Overview

Evaluates telemetry against alert rules and manages alert acknowledgment and resolution. Stores notification preferences and tracks delivery attempts. Local Compose uses a simulated notification provider.

**Stack**  Java 25, Spring Boot, PostgreSQL, Flyway.

## Run locally

Requirements are Docker Desktop with the Linux engine and PowerShell 7. Keep the SmartGN repositories as siblings for the integrated stack.

```powershell
./scripts/start-local.ps1
```

Run `./scripts/start-local.ps1 -Integrated` to start all 11 services through the integrated Compose.

| Endpoint | Local URL |
| --- | --- |
| Swagger UI | [Open](http://localhost:8086/swagger-ui/index.html) |
| OpenAPI | [JSON](http://localhost:8086/v3/api-docs) |
| Health | [Open](http://localhost:8086/actuator/health) |

## Configuration

Standalone settings use the variables in `.env.example`. Copy it to an ignored `.env` when the local startup script requires it. Integrated credentials and generated files are stored under `.local` in the Gateway repository.

## Verification

```powershell
./mvnw.cmd test
./scripts/smoke-local.ps1
```

Run the integrated smoke from the Gateway repository to verify cross-service workflows.

## Repository layout

- `src/main` contains the service implementation.
- `src/test` contains service tests.
- `docs` contains service documentation.
- `scripts` contains local startup and verification commands.
- `tools` contains verification helpers and supporting assets.
- `fixtures/db.json` defines demo fixtures for local verification.

## Documentation

- [Local development](docs/local-development.md)
- [Architecture](docs/architecture.md)
- [Contracts](docs/contracts.md)
- [OpenAPI export](docs/openapi.json)
- [Frontend integration](docs/frontend-integration.md)
- [Service overview](docs/service-overview.md)

## License

Distributed under the [MIT License](LICENSE).
