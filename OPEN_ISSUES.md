# Open issues

Known gaps of CosmoScan and possible approaches to close them. None of these are in progress: the
list documents the current limitations and the intended direction, so that the next contributor
does not have to rediscover them.

Back to [README](README.md).

| # | Title | Area | Priority |
|---|---|---|---|
| 1 | [Submission is one synchronous chain across three services](#1-submission-is-one-synchronous-chain-across-three-services) | Architecture | High |
| 2 | [No authentication or authorization](#2-no-authentication-or-authorization) | Security | High |
| 3 | [Internal endpoints are reachable through published ports](#3-internal-endpoints-are-reachable-through-published-ports) | Security | High |
| 4 | [No CI pipeline; e2e tests and Docker build never run automatically](#4-no-ci-pipeline-e2e-tests-and-docker-build-never-run-automatically) | CI/CD | High |
| 5 | [Technical check repeats the upload validation](#5-technical-check-repeats-the-upload-validation) | Domain | Medium |
| 6 | [File type is trusted from the client](#6-file-type-is-trusted-from-the-client) | Security | Medium |
| 7 | [Files are stored on a local disk](#7-files-are-stored-on-a-local-disk) | Storage | Medium |
| 8 | [No integration tests with a real database](#8-no-integration-tests-with-a-real-database) | Testing | Medium |
| 9 | [File is downloaded twice during analysis](#9-file-is-downloaded-twice-during-analysis) | Performance | Low |
| 10 | [Report is stored twice](#10-report-is-stored-twice) | Storage | Low |
| 11 | [Slow Docker builds](#11-slow-docker-builds) | Build | Low |
| 12 | [Credentials are hardcoded in docker-compose.yml](#12-credentials-are-hardcoded-in-docker-composeyml) | Config | Low |
| 13 | [Word cloud quality](#13-word-cloud-quality) | Domain | Low |
| 14 | [Postman collection is not in the repository](#14-postman-collection-is-not-in-the-repository) | Testing | Low |

---

## 1. Submission is one synchronous chain across three services

**Area:** Architecture | **Priority:** High

**Current state.** `POST /works` uploads the file to file-storing, saves the work, calls
`POST /internal/analysis`, which downloads the file again, runs Tika text extraction and builds the
word cloud, all inside one HTTP request. If anything fails after the upload, submission-service
rolls back the work row and deletes the stored file (compensating action). This does not cover
everything:

- a report and a word cloud already saved by analysis-service are not rolled back;
- if the submission transaction fails to commit after a successful analysis, the file and the
  analysis data stay;
- if the compensating delete itself fails, the file stays (only a log line);
- a large document keeps the client request open for the whole extraction.

**Proposed approach.**
- Make analysis asynchronous: `POST /works` stores the file, saves the work with status
  `PENDING` and returns `202 Accepted` with the work id.
- Publish a `WorkSubmitted` event through a transactional outbox table in submission-db, delivered
  to analysis-service by a message broker (RabbitMQ or Kafka) or by a polling relay.
- analysis-service processes the event idempotently (keyed by `workId`) and publishes
  `WorkAnalyzed` / `WorkAnalysisFailed`; submission-service updates the work status.
- Add `GET /works/{workId}` so the client can poll the status (`PENDING`, `ANALYZED`, `FAILED`).
- Replace the immediate compensation with a cleanup job for files whose work never reached a final
  state.

**Done when.** `POST /works` returns without waiting for the analysis, every work ends in a final
status, and a failure in any service leaves no orphan file, report or word cloud.

---

## 2. No authentication or authorization

**Area:** Security | **Priority:** High

**Current state.** Every public endpoint is open. Anyone can submit works, read any report or word
cloud and download any file by its id. Student names are personal data.

**Proposed approach.**
- Put an OAuth2 / OpenID Connect provider in front (Keycloak in Compose for local runs).
- The gateway validates JWTs (`spring-boot-starter-oauth2-resource-server`) and forwards the token.
- Roles: `STUDENT` (submit, read own works), `TEACHER` (read all reports and word clouds).
- Store the submitting user's id on the work and check ownership on reads.
- web-ui logs in through the provider and sends the token with each request.

**Done when.** Requests without a valid token get `401`, a student cannot read another student's
work, and the e2e tests run with test users.

---

## 3. Internal endpoints are reachable through published ports

**Area:** Security | **Priority:** High

**Current state.** The gateway does not route `/internal/analysis` and `/internal/files/{fileId}`,
but `docker-compose.yml` publishes ports 8091-8093 to the host. Anyone who can reach the host can
call the services directly, including `DELETE /internal/files/{fileId}`.

**Proposed approach.**
- Remove the `ports` mappings of the three services (and of the databases) from the default
  Compose file; keep them in a `docker-compose.override.yml` or a `debug` profile for local work.
- Optionally protect internal calls with a shared secret header or mTLS between services.

**Done when.** With the default Compose file only the gateway (8090) and web-ui (8094) are
reachable from the host.

---

## 4. No CI pipeline; e2e tests and Docker build never run automatically

**Area:** CI/CD | **Priority:** High

**Current state.** There is no `.github/workflows`. Unit tests and the 100% coverage gate run only
on a developer machine. The e2e tests in `e2e-tests/` have never been run, and the Dockerfiles and
Compose health checks have not been verified by a build.

**Proposed approach.**
- GitHub Actions workflow on `push` and `pull_request`:
  - job `build`: `actions/setup-java` (Temurin 17, Gradle cache), `./gradlew build`, upload the
    JaCoCo HTML reports as an artifact;
  - job `e2e` (needs `build`): `docker compose up --build -d --wait`, then
    `./gradlew :e2e-tests:e2eTest`, upload test reports and `docker compose logs` on failure,
    `docker compose down -v` at the end.
- Make both jobs required checks for `master`.

**Done when.** Every pull request shows a green `build` and `e2e` check, and a broken Dockerfile or
failing e2e test blocks the merge.

---

## 5. Technical check repeats the upload validation

**Area:** Domain | **Priority:** Medium

**Current state.** `TechnicalCheckService` checks extension, size and ZIP content type, which are the
same rules that submission-service and file-storing-service already enforce on upload. Because a
file that breaks them never gets stored, a report from the normal flow is always `ACCEPTED`;
`NEEDS_REVISION` is reachable only by calling `/internal/analysis` directly.

**Proposed approach.** Give the technical check its own rules that look at the content, for
example:
- the real file type (see #6) matches the extension;
- the document has extractable text (not a scanned image without OCR);
- minimum / maximum text length or page count;
- the document is not password-protected or corrupted (Tika reports this).

Make the rule set configurable and keep the upload validation as a cheap first filter.

**Done when.** A stored but unsuitable document (for example an empty or image-only PDF) gets
`NEEDS_REVISION` with a clear remark.

---

## 6. File type is trusted from the client

**Area:** Security | **Priority:** Medium

**Current state.** The allowed type is decided by the file name extension and the
`Content-Type` sent by the client. A ZIP or an executable renamed to `work.txt` with
`Content-Type: text/plain` is accepted.

**Proposed approach.**
- Detect the type from the content with `Tika.detect(bytes, fileName)` in file-storing-service and
  reject files whose detected type is not PDF, DOCX or plain text.
- Store the detected type in the metadata instead of the client value.
- Optionally scan uploads with an antivirus (ClamAV container).

**Done when.** A renamed archive or binary is rejected with `400` even with a valid extension and
content type.

---

## 7. Files are stored on a local disk

**Area:** Storage | **Priority:** Medium

**Current state.** file-storing-service writes each file and a `metadata.json` into a directory on
its own volume. Only one instance can run, there is no replication or backup, and metadata cannot
be queried.

**Proposed approach.**
- Store file content in S3-compatible object storage (MinIO in Compose) behind a `FileStorage`
  interface, so tests can keep using the file system implementation.
- Move metadata into a database table (file-storing gets its own schema) or into object metadata.

**Done when.** file-storing-service can run as several instances and a restart or a lost container
volume does not lose files.

Related: #1 (cleanup job), #6 (detected type in metadata).

---

## 8. No integration tests with a real database

**Area:** Testing | **Priority:** Medium

**Current state.** Unit tests mock the repositories, and the e2e tests need the whole stack. JPA
mappings, Flyway migrations and queries such as `findByWorkIdOrderByCheckedAtDesc` are checked only
when a service starts against a real PostgreSQL.

**Proposed approach.**
- Add `@DataJpaTest` / `@SpringBootTest` tests with Testcontainers PostgreSQL in submission-service
  and analysis-service (`spring-boot-testcontainers`, `@ServiceConnection`).
- Cover: migrations apply on an empty database, entities round-trip, repository queries, the
  transaction rollback in `WorkSubmissionService`.
- Run them in a separate Gradle source set or tag so the fast unit test run stays fast; run both in
  CI (#4).

**Done when.** A broken migration or JPA mapping fails a test instead of the service start.

---

## 9. File is downloaded twice during analysis

**Area:** Performance | **Priority:** Low

**Current state.** `FileStoringClient.fileExists` checks existence with a full `GET /files/{id}`
(the body is downloaded and discarded), and `WordCloudService` then downloads the same file again.

**Proposed approach.** Download the file once in `WorkAnalysisService`, treat `404` as "file not
found", and pass the bytes to the word cloud step. If an existence check is still needed elsewhere,
use `HEAD /files/{id}`.

**Done when.** One analysis makes one request to file-storing-service.

---

## 10. Report is stored twice

**Area:** Storage | **Priority:** Low

**Current state.** Each technical report is saved in analysis-db and also written as a JSON file to
`STORAGE_REPORTS_PATH`. Nothing reads the JSON files, and they are not cleaned up.

**Proposed approach.** Decide whether the files have a consumer (archive, export). If not, remove
`ReportFileWriter` and the `report_file_path` column (new Flyway migration). If yes, document the
consumer and move the files to object storage (#7).

**Done when.** Every stored copy of a report has a documented reader.

---

## 11. Slow Docker builds

**Area:** Build | **Priority:** Low

**Current state.** Each service Dockerfile copies all modules and runs a separate Gradle build, so
`docker compose up --build` resolves dependencies and compiles several times. The web-ui image runs
the whole unit test suite only to produce JaCoCo reports.

**Proposed approach.**
- Build all JARs once (CI or one shared builder stage) and make the service images only copy their
  JAR, or use a Gradle dependency cache mount (`RUN --mount=type=cache,target=/root/.gradle`).
- Generate JaCoCo reports in CI (#4) and publish them (artifact or GitHub Pages) instead of
  running tests inside the web-ui image.

**Done when.** A rebuild after a change in one service rebuilds only that service, and web-ui builds
without running tests.

---

## 12. Credentials are hardcoded in docker-compose.yml

**Area:** Config | **Priority:** Low

**Current state.** The PostgreSQL user and password (`cosmoscan` / `cosmoscan`) are written in
`docker-compose.yml` and in the `application.yml` defaults.

**Proposed approach.** Read them from a `.env` file (with `.env.example` committed and `.env`
ignored) or Docker secrets; keep the local defaults only for development profiles.

**Done when.** No password is committed outside of an example file.

---

## 13. Word cloud quality

**Area:** Domain | **Priority:** Low

**Current state.** `WordFrequencyService` counts exact lower-case word forms with small English and
Russian stop word lists. Inflected forms ("orbit", "orbits"; Russian case endings) are counted as
different words, and other languages get no stop words.

**Proposed approach.**
- Add stemming or lemmatization (Apache Lucene analyzers: `EnglishAnalyzer`, `RussianAnalyzer`).
- Detect the document language (Tika `LanguageDetector`) and pick the analyzer and stop words.
- Make the number of terms and the minimum word length configurable.

**Done when.** Different forms of one word are merged in the cloud for English and Russian texts.

---

## 14. Postman collection is not in the repository

**Area:** Testing | **Priority:** Low

**Current state.** `postman/` holds only test scripts that are pasted into requests by hand; the
collection itself has to be rebuilt in every Postman installation (see `postman/README.md`).

**Proposed approach.** Export the collection and an environment file to `postman/`, and optionally
run it with Newman in CI (#4) next to the e2e tests. If the Postman checks only duplicate the e2e
tests, consider keeping just the e2e tests.

**Done when.** The collection can be imported in one step, or the Postman folder is removed in favor
of the e2e tests.
