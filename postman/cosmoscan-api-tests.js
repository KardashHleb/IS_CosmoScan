/**
 * CosmoScan — скрипты для вкладки Tests в Postman.
 *
 * Как использовать:
 * 1. Создайте Collection «CosmoScan».
 * 2. Variables коллекции (Initial / Current):
 *    - gatewayUrl     = http://localhost:8090
 *    - submissionUrl  = http://localhost:8091
 *    - fileStoringUrl = http://localhost:8092
 *    - analysisUrl    = http://localhost:8093
 *    - workId         = (пусто, заполнится после Submit work)
 *    - fileId         = (пусто, заполнится после Upload / Submit)
 *    - reportId       = (пусто, опционально)
 * 3. docker compose up должен быть запущен.
 * 4. Скопируйте блок Tests нужного запроса во вкладку Tests соответствующего request.
 *
 * Рекомендуемый порядок запуска (Collection Runner): сверху вниз по папкам.
 */

// =============================================================================
// ПАПКА: API Gateway (публичный клиентский доступ, порт 8090)
// =============================================================================

// -----------------------------------------------------------------------------
// 1. POST {{gatewayUrl}}/works — Submit work (полный сценарий)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - New Request → имя: «Gateway — Submit work»
 * - Method: POST
 * - URL: {{gatewayUrl}}/works
 * - Body → form-data:
 *     studentFullName (Text) = Ivan Ivanov
 *     file (File)            = выберите test.pdf / test.docx / test.txt (≤ 1 MiB)
 * - Вкладка Tests → вставьте скрипт ниже (блок «Submit work»).
 * - Сохраните в папку «API Gateway».
 */
// --- Submit work (Gateway) ---
pm.test("Status is 201 Created", function () {
    pm.response.to.have.status(201);
});

pm.test("Response is JSON", function () {
    pm.response.to.have.header("Content-Type");
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

const submitBody = pm.response.json();

pm.test("Work has id and fileId", function () {
    pm.expect(submitBody.id).to.be.a("string").and.not.empty;
    pm.expect(submitBody.fileId).to.be.a("string").and.not.empty;
});

pm.test("Student name is present", function () {
    pm.expect(submitBody.studentFullName).to.eql("Ivan Ivanov");
});

pm.test("Technical report is included", function () {
    pm.expect(submitBody.technicalReport).to.be.an("object");
    pm.expect(submitBody.technicalReport.status).to.be.oneOf(["ACCEPTED", "NEEDS_REVISION"]);
    pm.expect(submitBody.technicalReport.remarks).to.be.an("array");
});

pm.collectionVariables.set("workId", submitBody.id);
pm.collectionVariables.set("fileId", submitBody.fileId);
if (submitBody.technicalReport && submitBody.technicalReport.id) {
    pm.collectionVariables.set("reportId", submitBody.technicalReport.id);
}

// -----------------------------------------------------------------------------
// 2. GET {{gatewayUrl}}/works/{{workId}}/reports — List reports
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - New Request → «Gateway — Get reports by workId»
 * - Method: GET
 * - URL: {{gatewayUrl}}/works/{{workId}}/reports
 *   (workId появится после успешного Submit work)
 * - Tests → скрипт «Get reports (Gateway)».
 * - Запускайте после Submit work.
 */
// --- Get reports (Gateway) ---
pm.test("Status is 200 OK", function () {
    pm.response.to.have.status(200);
});

const reports = pm.response.json();

pm.test("Response is an array", function () {
    pm.expect(reports).to.be.an("array");
});

pm.test("At least one report exists for submitted work", function () {
    pm.expect(reports.length).to.be.at.least(1);
});

pm.test("Report references the work", function () {
    const workId = pm.collectionVariables.get("workId");
    reports.forEach(function (report) {
        pm.expect(report.workId).to.eql(workId);
        pm.expect(report.status).to.be.oneOf(["ACCEPTED", "NEEDS_REVISION"]);
    });
});

if (reports.length > 0) {
    pm.collectionVariables.set("reportId", reports[0].id);
}

// -----------------------------------------------------------------------------
// 2b. GET {{gatewayUrl}}/works/{{workId}}/word-cloud — Word cloud data
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - New Request → «Gateway — Get word cloud by workId»
 * - Method: GET
 * - URL: {{gatewayUrl}}/works/{{workId}}/word-cloud
 * - Tests → скрипт «Get word cloud (Gateway)».
 * - Запускайте после Submit work.
 */
// --- Get word cloud (Gateway) ---
pm.test("Status is 200 OK", function () {
    pm.response.to.have.status(200);
});

const wordCloud = pm.response.json();

pm.test("Word cloud references the work", function () {
    const workId = pm.collectionVariables.get("workId");
    pm.expect(wordCloud.workId).to.eql(workId);
    pm.expect(wordCloud.fileId).to.be.a("string").and.not.empty;
});

pm.test("Word cloud has status and terms array", function () {
    pm.expect(wordCloud.status).to.be.oneOf(["READY", "NO_TEXT", "FAILED"]);
    pm.expect(wordCloud.terms).to.be.an("array");
});

pm.test("READY cloud has weighted terms", function () {
    if (wordCloud.status === "READY" && wordCloud.terms.length > 0) {
        pm.expect(wordCloud.terms[0].text).to.be.a("string").and.not.empty;
        pm.expect(wordCloud.terms[0].weight).to.be.a("number").and.above(0);
    }
});

// -----------------------------------------------------------------------------
// 3. POST {{gatewayUrl}}/files — Upload file
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - New Request → «Gateway — Upload file»
 * - Method: POST
 * - URL: {{gatewayUrl}}/files
 * - Body → form-data:
 *     file (File) = test.txt
 * - Tests → скрипт «Upload file (Gateway)».
 */
// --- Upload file (Gateway) ---
pm.test("Status is 201 Created", function () {
    pm.response.to.have.status(201);
});

const uploaded = pm.response.json();

pm.test("Stored file metadata is returned", function () {
    pm.expect(uploaded.fileId).to.be.a("string").and.not.empty;
    pm.expect(uploaded.originalFileName).to.be.a("string").and.not.empty;
    pm.expect(uploaded.fileSizeBytes).to.be.a("number");
});

pm.collectionVariables.set("fileId", uploaded.fileId);

// -----------------------------------------------------------------------------
// 4. GET {{gatewayUrl}}/files/{{fileId}} — Download file
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - New Request → «Gateway — Download file»
 * - Method: GET
 * - URL: {{gatewayUrl}}/files/{{fileId}}
 * - Tests → скрипт «Download file (Gateway)».
 * - Send and Download: в Postman ответ может быть файлом, не JSON.
 */
// --- Download file (Gateway) ---
pm.test("Status is 200 OK", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has Content-Disposition attachment", function () {
    pm.expect(pm.response.headers.get("Content-Disposition")).to.include("attachment");
});

pm.test("Body is not empty", function () {
    pm.expect(pm.response.responseSize).to.be.above(0);
});

// =============================================================================
// ПАПКА: Submission Service (прямой доступ, порт 8091)
// =============================================================================

// -----------------------------------------------------------------------------
// 5. POST {{submissionUrl}}/works — Submit work (direct)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - Папка «Submission Service (8091)»
 * - Дублируйте запрос Submit work, URL: {{submissionUrl}}/works
 * - Тот же form-data, что и для Gateway.
 * - Tests → скрипт «Submit work (Gateway)» (тот же блок, что в п.1).
 */
// Используйте блок «Submit work (Gateway)» выше.

// =============================================================================
// ПАПКА: File Storing Service (прямой доступ, порт 8092)
// =============================================================================

// -----------------------------------------------------------------------------
// 6. POST {{fileStoringUrl}}/files — Upload file (direct)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - Папка «File Storing Service (8092)»
 * - POST {{fileStoringUrl}}/files, form-data file
 * - Tests → «Upload file (Gateway)» (тот же скрипт, что в п.3).
 */

// -----------------------------------------------------------------------------
// 7. GET {{fileStoringUrl}}/files/{{fileId}} — Download file (direct)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - GET {{fileStoringUrl}}/files/{{fileId}}
 * - Tests → «Download file (Gateway)» (тот же скрипт, что в п.4).
 */

// =============================================================================
// ПАПКА: Analysis Service (прямой доступ, порт 8093)
// =============================================================================

// -----------------------------------------------------------------------------
// 8. POST {{analysisUrl}}/internal/analysis — Run analysis (internal)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - Папка «Analysis Service (8093)»
 * - Method: POST
 * - URL: {{analysisUrl}}/internal/analysis
 * - Headers: Content-Type = application/json
 * - Body → raw → JSON (подставьте реальные UUID после Submit work):
 *   {
 *     "workId": "{{workId}}",
 *     "fileId": "{{fileId}}",
 *     "studentFullName": "Ivan Ivanov",
 *     "originalFileName": "test.txt",
 *     "contentType": "text/plain",
 *     "fileSizeBytes": 20
 *   }
 * - Tests → скрипт «Internal analysis».
 * - Примечание: через Gateway этот endpoint недоступен (by design).
 */
// --- Internal analysis ---
pm.test("Status is 201 Created", function () {
    pm.response.to.have.status(201);
});

const report = pm.response.json();

pm.test("Report has required fields", function () {
    pm.expect(report.id).to.be.a("string").and.not.empty;
    pm.expect(report.workId).to.eql(pm.collectionVariables.get("workId"));
    pm.expect(report.fileId).to.eql(pm.collectionVariables.get("fileId"));
    pm.expect(report.status).to.be.oneOf(["ACCEPTED", "NEEDS_REVISION"]);
    pm.expect(report.remarks).to.be.an("array");
});

pm.collectionVariables.set("reportId", report.id);

// -----------------------------------------------------------------------------
// 9. GET {{analysisUrl}}/works/{{workId}}/reports — List reports (direct)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - GET {{analysisUrl}}/works/{{workId}}/reports
 * - Tests → «Get reports (Gateway)» (тот же скрипт, что в п.2).
 */

// =============================================================================
// ПАПКА: Негативные сценарии (опционально, Gateway)
// =============================================================================

// -----------------------------------------------------------------------------
// 10. POST {{gatewayUrl}}/works — Invalid file (zip or > 1 MiB)
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - POST {{gatewayUrl}}/works
 * - form-data: studentFullName + file.zip (или файл > 1 MB)
 * - Tests → скрипт «Invalid file upload».
 */
// --- Invalid file upload ---
pm.test("Status is 400 Bad Request", function () {
    pm.response.to.have.status(400);
});

pm.test("Error body is present", function () {
    const contentType = pm.response.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
        const body = pm.response.json();
        pm.expect(body.detail || body.title || body.message).to.be.a("string");
    }
});

// -----------------------------------------------------------------------------
// 11. GET {{gatewayUrl}}/files/00000000-0000-0000-0000-000000000000 — Not found
// -----------------------------------------------------------------------------
/*
 * Шаги в Postman:
 * - GET {{gatewayUrl}}/files/00000000-0000-0000-0000-000000000000
 * - Tests → скрипт «File not found».
 */
// --- File not found ---
pm.test("Status is 404 Not Found", function () {
    pm.response.to.have.status(404);
});
