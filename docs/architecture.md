# Architecture

[Back to README](../README.md) | [All diagrams](diagrams.md)

Architecture diagram: [diagrams.md](diagrams.md#system-architecture).

## What the system does

### 1. Microservice decomposition

The project consists of four runnable Gradle modules and two supporting ones:

- **gateway** - Spring Cloud Gateway: routes to the services, Swagger UI under `/swagger/{service}/`,
  Actuator health endpoint.
- **submission-service** - REST API for submitting works (`multipart/form-data`), JPA + Flyway,
  PostgreSQL.
- **file-storing-service** - file upload, download and internal delete; format and size
  validation; files are stored in a directory on disk.
- **analysis-service** - technical check, reports in the database and as JSON on disk, word cloud
  data.
- **file-validation** - plain Java library with the file rules (allowed formats, size limit, ZIP
  ban) shared by the three services. Before it existed, the same validator was copied into
  submission-service and file-storing-service.
- **e2e-tests** - end-to-end API tests that run against a running stack (see [testing.md](testing.md)).

Every runnable module has its own `Dockerfile` and a service in `docker-compose.yml`. All
application containers have a Compose health check: the Spring services and the gateway use
`/actuator/health`, web-ui is checked with an HTTP request to nginx. Containers start in
`depends_on` order and wait for their dependencies to become `healthy`.

### 2. End-to-end "Submit work" flow

`POST /works` runs this chain:

1. File validation (extension `pdf` / `docx` / `txt`, at most **1 MiB**, no ZIP).
2. The file is stored in **file-storing-service**, which returns a `fileId`.
3. Work metadata is written to **submission-db** (`workId`, student name, `fileId`, file name, size,
   content type).
4. Synchronous call to **analysis-service** (`POST /internal/analysis`).
5. The client receives a `WorkResponse` with a short technical report.

If step 3 or 4 fails, the work row is rolled back by the transaction and submission-service deletes
the already stored file (`DELETE /internal/files/{fileId}`), so no orphan file is left behind. The
original error is returned to the client.

### 3. Technical analysis (analysis-service)

- Checks that the file exists in file-storing (`GET /files/{fileId}`).
- **TechnicalCheckService** checks the metadata (size, extension, content type) against the shared
  file rules, without reading the content.
- Report statuses: `ACCEPTED` / `NEEDS_REVISION`, plus a list of `remarks`.
- The report is saved in PostgreSQL and also written as a JSON file (`ReportFileWriter`).

### 4. Word cloud

Built in **analysis-service** during the same analyze call:

- The file is downloaded from file-storing.
- Text is extracted with **Apache Tika 2.9.2** (Apache-2.0 license).
- Word frequencies are counted by the project's own **WordFrequencyService** (English stop words,
  minimum word length 3; words in any alphabet are counted, but stop words are filtered only for
  English).
- Results are stored in the `work_word_clouds` / `work_word_cloud_terms` tables.
- Statuses: `READY`, `NO_TEXT`, `FAILED` (an extraction error does not cancel the submission).

Public read: `GET /works/{workId}/word-cloud` returns JSON with a `{ "text", "weight" }` array for
client-side rendering (for example with wordcloud2.js).

### 5. API Gateway

Routes public requests on port **8090**:

- `POST /works` -> submission-service
- `GET /works/{workId}/reports` -> analysis-service
- `GET /works/{workId}/word-cloud` -> analysis-service
- `POST /files`, `GET /files/{fileId}` -> file-storing-service
- `GET /actuator/health` - the gateway's own health

### 6. Quality and API documentation

- **OpenAPI (springdoc)** on every service.
- **Unit tests** with a JaCoCo gate of **100%** instructions, lines and branches for gateway,
  submission-service, file-storing-service, analysis-service and file-validation (`check` task).
- **End-to-end API tests** in `e2e-tests/` (JUnit 5 + REST Assured) against the gateway.
- **Postman** test scripts in `postman/`, described in [postman/README.md](../postman/README.md).
- **web-ui** - single-page portal (HTML/CSS/JS + nginx in Docker); the word cloud is drawn in the
  browser with **wordcloud2.js**, the terms are computed by **analysis-service** in Java.

---

## Service interaction scenarios

### TS-1. Synchronous orchestration on submit

| # | From | To | Method | Data |
|---|---|---|--------|--------|
| 1 | submission | file-storing | `POST /files` | `MultipartFile` |
| 2 | submission | analysis | `POST /internal/analysis` | JSON `AnalyzeWorkRequest` |
| 3 | analysis | file-storing | `GET /files/{fileId}` | existence check + download for the word cloud |
| 4 | submission | file-storing | `DELETE /internal/files/{fileId}` | only if step 2 or saving the work failed |

**AnalyzeWorkRequest** (body of the internal call):

```json
{
  "workId": "uuid",
  "fileId": "uuid",
  "studentFullName": "string",
  "originalFileName": "work.pdf",
  "contentType": "application/pdf",
  "fileSizeBytes": 12345
}
```

**Failure handling:** if the file is missing in file-storing, analysis returns `400` and submission
passes the `400` to the client. Whenever saving the work or the analysis call fails, the JPA
transaction in submission rolls back the `works` row and submission deletes the stored file
(compensating action). If that delete also fails, the error is logged and attached to the original
exception as suppressed; the file then stays in storage (see
[Known limitations](../README.md#known-limitations)).

---

### TS-2. Technical check (without parsing the text)

Diagram: [TS-2 in diagrams.md](diagrams.md#ts-2-technical-check).

The rules come from the shared **file-validation** module and are the same as on upload:
**pdf, docx, txt**, at most 1 MiB, no ZIP.

---

### TS-3. Building the word cloud

Diagram: [TS-3 in diagrams.md](diagrams.md#ts-3-building-the-word-cloud).

---

### TS-4. Reads through the gateway (without submission)

| Client request | Target service | Note |
|----------------|-------------------|------------|
| `GET /works/{id}/reports` | analysis-service | Read-only from analysis-db |
| `GET /works/{id}/word-cloud` | analysis-service | 404 if the analysis has not run yet |
| `GET /files/{id}` | file-storing-service | Binary file body |

submission-service is **not** involved in these scenarios.

---

### TS-5. Data storage per service

The **logical** model below spans two separate databases: `works` in submission-db, reports and
word cloud in analysis-db. There are no foreign keys between the PostgreSQL instances.

Diagram: [TS-5 in diagrams.md](diagrams.md#ts-5-data-model).

**Outside the databases:**

| Service | Artifact | Path (default) |
|--------|----------|---------------------|
| file-storing | work files + `metadata.json` per file | `STORAGE_FILES_PATH` / Docker volume |
| analysis | report JSON files | `STORAGE_REPORTS_PATH` |

The databases are linked only logically through shared `workId` and `fileId` (UUID).
