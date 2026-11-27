# Testing

[Back to README](../README.md) | [All diagrams](diagrams.md)

| Level | Where | Runs in `build` | Needs the stack |
|-------|-------|-----------------|-----------------|
| Unit tests | `*/src/test` | yes | no |
| End-to-end API tests | `e2e-tests/` | no (compiled only) | yes |
| Postman checks | `postman/` | no | yes |

## Unit tests and coverage

Unit tests use JUnit 5, Mockito, AssertJ, standalone MockMvc (controllers), `MockRestServiceServer`
(HTTP clients) and one `@WebMvcTest` for request validation. They need no database or network.

JaCoCo is a hard gate: `check` fails unless **instructions, lines and branches are 100% covered** in
gateway, submission-service, file-storing-service, analysis-service and file-validation. There are
no exclusions.

```bash
# All unit tests + JaCoCo gate (Linux/macOS)
./gradlew check

# Windows
gradlew.bat check

# One module
./gradlew :analysis-service:test

# Copy the JaCoCo HTML reports into web-ui/reports/ (already part of the web-ui Docker build)
./gradlew copyJacocoReportsForWeb
```

HTML reports: `<module>/build/reports/jacoco/test/html/index.html`.

## End-to-end API tests

`e2e-tests/` holds black-box tests (JUnit 5 + REST Assured) that call the API through the gateway,
exactly like a client:

| Test class | What it checks |
|------------|----------------|
| `SubmitWorkE2eTest` | UC-1 to UC-3 for `.txt`, `.docx` and `.pdf`: submit, report history, word cloud with the expected top word, download of the stored file |
| `SubmitWorkValidationE2eTest` | Rejected files (format, missing extension, ZIP, empty, over 1 MiB), the 1 MiB boundary, missing or blank student name, missing file |
| `ReadApiE2eTest` | Reads for unknown works, malformed UUID, `NO_TEXT` word cloud |
| `FileStorageE2eTest` | UC-4: upload/download round trip, validation on direct upload, unknown file |
| `GatewayE2eTest` | Gateway health, Swagger routing, internal endpoints not exposed, unknown route |

Fixtures with one dominant word each are in `e2e-tests/src/test/resources/files/`.

The tests are not part of `build`/`check` (the regular `test` task of the module is disabled). Run
them against a running stack:

```bash
docker compose up --build -d
./gradlew :e2e-tests:e2eTest

# another gateway address
./gradlew :e2e-tests:e2eTest -Pcosmoscan.baseUrl=http://my-host:8090
```

The gateway address can also be set with the `COSMOSCAN_BASE_URL` environment variable. The tests
are not wired into CI yet.

## Postman

Manual checks with Postman test scripts: see [postman/README.md](../postman/README.md).
