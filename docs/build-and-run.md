# Build and run

[Back to README](../README.md) | [All diagrams](diagrams.md)

## Build and dependencies

The project is built in two ways: **Docker Compose** (recommended for running the whole system) and
**Gradle on the host** (tests, JaCoCo, local development without containers).

### Docker Compose (`docker compose up --build`)

The command **builds** the images and **starts** all containers. On the first run Docker also
**pulls** the base images.

| Step | What happens |
|-----|----------------|
| 1 | Pulls `postgres:16-alpine`, `eclipse-temurin:17`, `nginx:1.27-alpine` (if not present locally) |
| 2 | **gateway**, **submission-service**, **file-storing-service**, **analysis-service** - multi-stage build: `./gradlew :<module>:bootJar -x test` runs inside the container |
| 3 | Gradle downloads the **Java dependencies** from **Maven Central** (Spring Boot, Spring Cloud Gateway, PostgreSQL driver, Flyway, Apache Tika, springdoc and transitive JARs) into the Docker layer cache |
| 4 | The runtime image contains only **JRE + the JAR**; sources are not copied into the final image |
| 5 | **web-ui** - multi-stage: the JDK stage runs `./gradlew check copyJacocoReportsForWeb` (unit tests, 100% JaCoCo gate, HTML into `web-ui/reports/`); the nginx stage gets `index.html`, `assets/` and the reports |
| 6 | Containers start in `depends_on` order; every container waits for its dependencies to be `healthy` |

The **web-ui** build takes longer than the others because it runs the tests. The service JARs are
built with `-x test` so the tests do not run several times.

Services and ports are listed in [the README](../README.md#services) and `docker-compose.yml`.

### Web portal dependencies (web-ui)

| Component | When it is fetched | From |
|-----------|---------------------|--------|
| Portal HTML, CSS, JS (`app.js`) | `web-ui` image build | Repository files -> nginx |
| PNG diagrams | `web-ui` image build | `web-ui/assets/images/` |
| JaCoCo HTML (`/reports/submission`, `files`, `analysis`, `gateway`, `file-validation`) | `web-ui` image build (Gradle `check` + `copyJacocoReportsForWeb`) | Generated inside Docker, nothing to do on the host |
| **wordcloud2.js** | **When the page is opened in the browser** | CDN [jsDelivr](https://cdn.jsdelivr.net/npm/wordcloud@1.2.2/src/wordcloud2.min.js) (MIT) |

The cloud is drawn in the browser; the **terms are computed** in **analysis-service** (Java). Work
data is not sent to the CDN.

To use the portal **without internet access**, put `wordcloud2.min.js` into
`web-ui/assets/vendor/` and change the `<script src="...">` in `index.html`.

The PNG diagrams are rendered from `web-ui/diagrams/plantuml/*.puml`. After changing a diagram:

```bash
./gradlew renderDiagrams
```

### Build on the host (Gradle)

```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

Downloads the same Maven dependencies into the local `~/.gradle` cache. The `check` task runs the
unit tests and the JaCoCo gate (100%). `copyJacocoReportsForWeb` copies the HTML reports into
`web-ui/reports/` for the JaCoCo section of the portal. The end-to-end tests are compiled but not
run by `build`.

To run the JARs without Docker you need local PostgreSQL and the variables from `application.yml`
(see [local development](#local-development-without-docker)).

---

## Running and checking

### Requirements

- **Docker Compose** - for the full stack (a JDK on the host is not required)
- **JDK 17+** and **Gradle (gradlew)** - for tests and builds without Docker

### Docker Compose

From the repository root:

```bash
# Build the images and start all services (logs in the console)
docker compose up --build

# Same, in the background
docker compose up --build -d

# Stop
docker compose down

# Stop and remove database and file volumes
docker compose down -v
```

**Windows (PowerShell):** the same commands; for builds without Docker use `gradlew.bat` instead of
`./gradlew`.

Wait until all containers are `healthy` (`docker compose ps`): both databases, the three services,
the gateway and web-ui.

| URL | Purpose |
|-----|------------|
| http://localhost:8094 | Web portal |
| http://localhost:8090 | API Gateway (portal and Swagger requests) |

Health checks:

```bash
curl http://localhost:8090/actuator/health
curl http://localhost:8091/actuator/health
curl http://localhost:8092/actuator/health
curl http://localhost:8093/actuator/health
```

Check through the gateway (after all containers are up, Swagger UI HTML is expected):

```bash
curl http://localhost:8090/swagger/submission/swagger-ui/index.html
```

### Local development (without Docker)

1. Start PostgreSQL (defaults from `application.yml`):

| Instance | Port | Database | User / Password |
|---------|------|----------|-----------------|
| submission | 5433 | `cosmoscan_submission` | `cosmoscan` / `cosmoscan` |
| analysis | 5434 | `cosmoscan_analysis` | `cosmoscan` / `cosmoscan` |

2. Start the services in this order: file-storing -> analysis -> submission -> gateway.
3. Environment variables are listed in `application.yml` of each module.

### Example: submit a work

```bash
curl -X POST http://localhost:8090/works \
  -F "studentFullName=Ivan Ivanov" \
  -F "file=@test.txt"
```

```bash
# use the UUID from the "id" field of the POST /works response
curl http://localhost:8090/works/00000000-0000-0000-0000-000000000001/word-cloud
```

---

## Web portal (web-ui)

The `web-ui/` folder is a static portal on **nginx** (container `web-ui`, port **8094**).

| Portal section | Description |
|----------------|----------|
| API connection | Gateway base URL (default `http://localhost:8090`) |
| Swagger / Health | Links to Swagger UI and to the Actuator health of the gateway and services |
| UC-1 | Submit a work (`POST /works`) |
| UC-2 | Technical reports by UUID |
| UC-3 | "Get word cloud" button + canvas |
| JaCoCo | Links to the HTML coverage reports |
| Architecture | System and UC-1 diagrams (`assets/images/`) |

Portal API requests go to the gateway (`localhost:8090`). The only external JS is **wordcloud2**
from a CDN (see [Build and dependencies](#build-and-dependencies)).

JaCoCo reports after `docker compose up --build`:
http://localhost:8094/reports/submission/index.html (and `files`, `analysis`, `gateway`,
`file-validation`).
