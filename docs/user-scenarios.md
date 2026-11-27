# User scenarios

[Back to README](../README.md) | [All diagrams](diagrams.md)

## UC-1. A student submits a work

**Actor:** student (or an external system)
**Goal:** upload a work and immediately get the technical check result.

| Step | Action |
|------|--------|
| 1 | The client sends `POST /works` to the gateway (`studentFullName` + file). |
| 2 | The system validates and stores the file. |
| 3 | The system runs the analysis. |
| 4 | The client receives `201` with the work `id`, `fileId` and a `technicalReport` block (`status`, `remarks`). |

**Result:** the work is registered; with `NEEDS_REVISION` the `remarks` list the reasons (size,
format and so on).

Sequence diagram: [UC-1 in diagrams.md](diagrams.md#uc-1-submit-a-work).

---

## UC-2. A teacher views the report history

**Actor:** teacher
**Goal:** get all technical reports of a `workId`.

| Step | Action |
|------|--------|
| 1 | `GET /works/{workId}/reports` through the gateway. |
| 2 | Response: an array of reports sorted by date (newest first). |

*Note:* a regular UC-1 submission creates one report; the array exists for repeated
`POST /internal/analysis` calls (internal scenario).

---

## UC-3. Work content visualization (word cloud)

**Actor:** teacher / analyst
**Goal:** see the word frequencies of the work text.

| Step | Action |
|------|--------|
| 1 | After submitting the work (UC-1), call `GET /works/{workId}/word-cloud`. |
| 2 | With `status: READY` use the `terms` array for the cloud in the UI. |
| 3 | With `NO_TEXT` / `FAILED` show a message to the user. |

---

## UC-4. Direct file upload (without a work)

**Actor:** integrator / tester
**Goal:** store a file and get a `fileId`.

| Step | Action |
|------|--------|
| 1 | `POST /files` with a multipart `file`. |
| 2 | `GET /files/{fileId}` - download through the gateway. |

*Note:* the regular flow is the UC-1 submission; UC-4 is useful for debugging file-storing.
