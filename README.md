# CosmoScan

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-2023.0-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?logo=flyway&logoColor=white)
![Apache Tika](https://img.shields.io/badge/Apache_Tika-2.9-D22128?logo=apache&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Compose-ready-2496ED?logo=docker&logoColor=white)
![nginx](https://img.shields.io/badge/nginx-1.27-009639?logo=nginx&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8-02303A?logo=gradle&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-springdoc-85EA2D?logo=swagger&logoColor=black)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![REST Assured](https://img.shields.io/badge/REST_Assured-e2e-5B8C32)
![Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen)

A system for receiving and pre-processing student works: file upload, technical check, report
generation and word cloud data for visualization.

## Services

Architecture diagram and all other diagrams: [docs/diagrams.md](docs/diagrams.md).

| Service | Port | Purpose |
|---------|------|---------|
| **web-ui** | 8094 | Static portal (nginx): scenarios UC-1 to UC-3, links to Swagger, health and JaCoCo |
| **gateway** | 8090 | Single API entry point, routing |
| **submission-service** | 8091 | Receives works, stores work metadata in its database |
| **file-storing-service** | 8092 | Stores files on disk |
| **analysis-service** | 8093 | Technical check, reports, word cloud |
| **submission-db** | 5433 | Works database |
| **analysis-db** | 5434 | Reports and word cloud database |

The internal endpoints `POST /internal/analysis` (analysis-service) and
`DELETE /internal/files/{fileId}` (file-storing-service) are **not** routed by the gateway; only
submission-service calls them.


## Quick start

Requirements: Docker Compose (for the full stack), JDK 17+ (for builds and tests without Docker).

```bash
# Build and start everything, then wait until all containers are healthy
docker compose up --build -d
docker compose ps
```

| URL | Purpose |
|-----|---------|
| http://localhost:8094 | Web portal |
| http://localhost:8090 | API Gateway |
| http://localhost:8090/swagger/submission/swagger-ui/index.html | Swagger UI (also `files`, `analysis`) |

```bash
# Submit a work through the gateway
curl -X POST http://localhost:8090/works -F "studentFullName=Ivan Ivanov" -F "file=@test.txt"

# Unit tests + 100% coverage gate
./gradlew check

# End-to-end API tests against the running stack
./gradlew :e2e-tests:e2eTest
```

## Documentation

| Document | Contents |
|----------|----------|
| [Diagrams](docs/diagrams.md) | Architecture, UC-1 sequence, technical check, word cloud, data model, portal diagrams |
| [Architecture](docs/architecture.md) | Modules, submit flow, analysis, word cloud, gateway; service interaction scenarios TS-1 to TS-5 |
| [User scenarios](docs/user-scenarios.md) | UC-1 to UC-4 |
| [API and data models](docs/api.md) | Public and internal endpoints, Swagger, response models |
| [Build and run](docs/build-and-run.md) | Docker Compose, Gradle, local development, web portal |
| [Testing](docs/testing.md) | Unit tests and coverage gate, end-to-end tests, Postman |
| [Dependencies and licenses](docs/dependencies.md) | Backend, portal and Docker image licenses |
| [Postman checks](postman/README.md) | Manual API checks with Postman test scripts |
| [Open issues](OPEN_ISSUES.md) | Known gaps and proposed approaches |

## Known limitations

The main ones are listed below. All known gaps, with proposed approaches, are described in
[OPEN_ISSUES.md](OPEN_ISSUES.md).

- **No authentication or authorization.** Every public endpoint is open; internal endpoints are
  protected only by not being routed through the gateway. The services' own ports (8091-8093) are
  published by Compose for debugging, so internal endpoints are reachable on those ports.
- **Compensation is best effort.** If deleting the stored file after a failed submit also fails, the
  file stays in storage (the error is logged). Data written by analysis-service before a later
  failure in submission (report, word cloud) is not rolled back.
- **Synchronous processing.** Text extraction and the word cloud run inside the `POST /works`
  request, so large documents make the request slower.
- **End-to-end tests are not run automatically** - there is no CI pipeline yet.


## Repository structure

```
CosmoScan/
├── gateway/
├── submission-service/
├── file-storing-service/
├── analysis-service/
├── file-validation/          shared file rules (library)
├── e2e-tests/                end-to-end API tests
├── docs/                     documentation (see the table above)
├── web-ui/
│   ├── index.html
│   ├── assets/
│   ├── diagrams/plantuml/
│   ├── nginx.conf
│   ├── Dockerfile
│   └── reports/
├── OPEN_ISSUES.md            known gaps and proposed approaches
├── postman/
│   ├── README.md
│   ├── cosmoscan-api-tests.js
│   └── files/
├── docker-compose.yml
├── build.gradle
└── settings.gradle
```
