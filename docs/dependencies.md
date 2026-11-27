# Dependencies and licenses

[Back to README](../README.md) | [All diagrams](diagrams.md)

Only **permissive** licenses (Apache-2.0, MIT, BSD-style) are used in runtime dependencies. There is
no copyleft (application-wide GPL) in direct runtime dependencies.

## Backend (Gradle / Maven Central)

| Dependency | Modules | License | Purpose |
|-------------|--------|----------|------------|
| Spring Boot 3.3.x | all services | **Apache-2.0** | REST, JPA, Validation, Actuator |
| Spring Cloud Gateway 2023.0.x | gateway | **Apache-2.0** | API Gateway |
| springdoc-openapi 2.6.x | submission, file-storing, analysis | **Apache-2.0** | Swagger UI / OpenAPI |
| Flyway | submission, analysis | **Apache-2.0** | Database migrations |
| PostgreSQL JDBC Driver | submission, analysis | **BSD-2-Clause** (PostgreSQL License) | Database driver |
| **Apache Tika 2.9.2** (`tika-core`, `tika-parsers-standard-package`) | analysis | **Apache-2.0** | Text extraction from pdf/docx/txt |
| reactive-streams | submission | **MIT** | Transitive dependency |
| JUnit 5, Mockito, AssertJ, Spring Boot Test | tests | **EPL-2.0** / **MIT** / **Apache-2.0** | Tests only, not in the production JARs |
| REST Assured | e2e-tests | **Apache-2.0** | End-to-end API tests only |
| JaCoCo | tests / `check` | **EPL-2.0** | Coverage reports |
| PlantUML (`plantuml-mit`) | build-time `renderDiagrams` | **MIT** | Renders the portal diagrams; not shipped |

**Own code (no third-party NLP libraries):** `WordFrequencyService`, `TechnicalCheckService`, stop
words and tokenization for the word cloud.

**Tika:** `tika-parsers-standard-package` brings transitive parser JARs (PDF and others); for strict
compliance in an organization, produce the full tree with
`./gradlew :analysis-service:dependencies`.

## Web portal (`web-ui/`)

| Component | Version | License | Purpose |
|-----------|--------|----------|------------|
| **wordcloud2.js** (npm `wordcloud`) | 1.2.2 | **MIT** | Draws the cloud on a `<canvas>` from the API JSON |
| nginx (Docker image) | 1.27-alpine | **BSD-2-Clause** | Serves the static files |

wordcloud2 is loaded in `index.html` from the jsDelivr CDN (`wordcloud@1.2.2`, MIT). Diagram
sources: `web-ui/diagrams/plantuml/`, PNGs in `web-ui/assets/images/`.

Own portal files: `index.html`, `assets/css/style.css`, `assets/js/app.js`.

## Docker images (Compose)

| Image | License (typical) |
|-------|---------------------|
| `eclipse-temurin:17` | **GPLv2 + Classpath Exception** (JRE) |
| `postgres:16-alpine` | **PostgreSQL License** |
| `nginx:1.27-alpine` | **BSD-2-Clause** |
