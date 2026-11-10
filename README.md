# Vendor resilience gateway

Monorepo for **Triage**, a self-healing verification gateway in front of unreliable third-party verification APIs.

| Directory        | Purpose                                      | Port |
|------------------|----------------------------------------------|------|
| `triage-gateway/`| Main Spring Boot gateway                     | 8080 |
| `mock-vendors/`  | Simulated PAN and bureau vendors (mocks only)| 8081 |
| `k6/`            | Load test scripts                            | -    |
| `drills/`        | Outage drill scripts                         | -    |

The two Spring Boot projects are independent Maven builds (no parent POM).

## Running locally

Start Postgres and Redis (requires Docker):

```
docker compose up -d
```

Then, from each project directory, in separate terminals:

```
..\triage-gateway\mvnw.cmd spring-boot:run    # mock-vendors, port 8081
.\mvnw.cmd spring-boot:run                    # triage-gateway, port 8080
```

The mock vendors expose a chaos control API at `/chaos/{pan-a|pan-b|bureau}`.
