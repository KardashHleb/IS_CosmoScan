# API and data models

[Back to README](../README.md) | [All diagrams](diagrams.md)

## Public API (through the gateway)

Base URL: `http://localhost:8090`

| Method | Path | Codes | Description |
|-------|------|------|----------|
| `POST` | `/works` | `201`, `400`, `413` | Submit a work (`studentFullName`, `file`). `400` for a disallowed file or blank name; a file over the 1 MiB upload limit can get `413` |
| `GET` | `/works/{workId}/reports` | `200`, `400` | List of technical reports (may be empty); `400` for a malformed UUID |
| `GET` | `/works/{workId}/word-cloud` | `200`, `404` | Word cloud data (`404` - the analysis has not run) |
| `POST` | `/files` | `201`, `400` | Upload a file |
| `GET` | `/files/{fileId}` | `200`, `404` | Download a file |
| `GET` | `/actuator/health` | `200` | Gateway health |

**Internal endpoints (not routed by the gateway):**

| Service | Method | Path | Called by |
|---------|--------|------|-----------|
| analysis-service | `POST` | `/internal/analysis` | submission-service |
| file-storing-service | `DELETE` | `/internal/files/{fileId}` | submission-service (compensation) |

**Swagger UI (through the gateway, springdoc 2.x):**

- `http://localhost:8090/swagger/submission/swagger-ui/index.html`
- `http://localhost:8090/swagger/files/swagger-ui/index.html`
- `http://localhost:8090/swagger/analysis/swagger-ui/index.html`

---

## Data models

### WorkResponse (submit response)

| Field | Type | Description |
|------|-----|----------|
| `id` | UUID | Work id |
| `studentFullName` | string | Student full name (trimmed) |
| `submittedAt` | ISO-8601 | Submission time |
| `originalFileName` | string | File name |
| `fileId` | UUID | Reference to file-storing |
| `fileSizeBytes` | number | Size |
| `contentType` | string | MIME type |
| `technicalReport` | object | `id`, `status`, `remarks`, `checkedAt` |

### WordCloudResponse

| Field | Type | Description |
|------|-----|----------|
| `workId` | UUID | Work |
| `fileId` | UUID | Source file |
| `generatedAt` | ISO-8601 | Calculation time |
| `status` | enum | `READY` \| `NO_TEXT` \| `FAILED` |
| `terms` | array | `{ "text": "...", "weight": N }` |

Errors are returned as RFC 7807 `ProblemDetail` JSON with a `detail` field.
