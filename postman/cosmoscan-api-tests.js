/**
 * CosmoScan - scripts for the Postman "Tests" tab.
 *
 * This file is not a collection. Each block below is pasted into the Tests tab of one request.
 * Full setup instructions: postman/README.md.
 *
 * Collection variables (Initial / Current value):
 *    - gatewayUrl     = http://localhost:8090
 *    - submissionUrl  = http://localhost:8091
 *    - fileStoringUrl = http://localhost:8092
 *    - analysisUrl    = http://localhost:8093
 *    - workId         = (empty, set by "Submit work")
 *    - fileId         = (empty, set by "Upload file" / "Submit work")
 *    - reportId       = (empty, optional)
 *
 * The stack must be running (docker compose up).
 * Recommended order in the Collection Runner: folders from top to bottom.
 */

// =============================================================================
// FOLDER: API Gateway (public client access, port 8090)
// =============================================================================

// -----------------------------------------------------------------------------
// 1. POST {{gatewayUrl}}/works - Submit work (full scenario)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Name: "Gateway - Submit work"
 * - Method: POST
 * - URL: {{gatewayUrl}}/works
 * - Body -> form-data:
 *     studentFullName (Text) = Ivan Ivanov
 *     file (File)            = a .pdf / .docx / .txt file up to 1 MiB
 *                              (e.g. e2e-tests/src/test/resources/files/cosmos-notes.txt)
 * - Tests tab: block "Submit work (Gateway)" below.
 * - Save to the "API Gateway" folder.
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
// 2. GET {{gatewayUrl}}/works/{{workId}}/reports - List reports
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Name: "Gateway - Get reports by workId"
 * - Method: GET
 * - URL: {{gatewayUrl}}/works/{{workId}}/reports
 *   (workId is set by a successful "Submit work")
 * - Tests tab: block "Get reports (Gateway)".
 * - Run after "Submit work".
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
// 2b. GET {{gatewayUrl}}/works/{{workId}}/word-cloud - Word cloud data
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Name: "Gateway - Get word cloud by workId"
 * - Method: GET
 * - URL: {{gatewayUrl}}/works/{{workId}}/word-cloud
 * - Tests tab: block "Get word cloud (Gateway)".
 * - Run after "Submit work".
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
// 3. POST {{gatewayUrl}}/files - Upload file
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Name: "Gateway - Upload file"
 * - Method: POST
 * - URL: {{gatewayUrl}}/files
 * - Body -> form-data:
 *     file (File) = postman/files/cyrillic-and-latin.txt
 * - Tests tab: block "Upload file (Gateway)".
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
// 4. GET {{gatewayUrl}}/files/{{fileId}} - Download file
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Name: "Gateway - Download file"
 * - Method: GET
 * - URL: {{gatewayUrl}}/files/{{fileId}}
 * - Tests tab: block "Download file (Gateway)".
 * - The response is a file, not JSON ("Send and Download" saves it).
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
// FOLDER: Submission Service (direct access, port 8091)
// =============================================================================

// -----------------------------------------------------------------------------
// 5. POST {{submissionUrl}}/works - Submit work (direct)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Folder "Submission Service (8091)"
 * - Duplicate "Submit work", URL: {{submissionUrl}}/works
 * - Same form-data as for the gateway.
 * - Tests tab: block "Submit work (Gateway)" (same as step 1).
 */
// Use the "Submit work (Gateway)" block above.

// =============================================================================
// FOLDER: File Storing Service (direct access, port 8092)
// =============================================================================

// -----------------------------------------------------------------------------
// 6. POST {{fileStoringUrl}}/files - Upload file (direct)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Folder "File Storing Service (8092)"
 * - POST {{fileStoringUrl}}/files, form-data file
 * - Tests tab: block "Upload file (Gateway)" (same as step 3).
 */

// -----------------------------------------------------------------------------
// 7. GET {{fileStoringUrl}}/files/{{fileId}} - Download file (direct)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - GET {{fileStoringUrl}}/files/{{fileId}}
 * - Tests tab: block "Download file (Gateway)" (same as step 4).
 */

// -----------------------------------------------------------------------------
// 7b. DELETE {{fileStoringUrl}}/internal/files/{{fileId}} - Delete file (internal)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - DELETE {{fileStoringUrl}}/internal/files/{{fileId}}
 * - Tests tab: block "Delete file (internal)".
 * - Run it last for this fileId: after it the file is gone.
 * - Note: submission-service calls this endpoint when a submit fails after the upload.
 *   It is not available through the gateway (by design).
 */
// --- Delete file (internal) ---
pm.test("Status is 204 No Content", function () {
    pm.response.to.have.status(204);
});

// =============================================================================
// FOLDER: Analysis Service (direct access, port 8093)
// =============================================================================

// -----------------------------------------------------------------------------
// 8. POST {{analysisUrl}}/internal/analysis - Run analysis (internal)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - Folder "Analysis Service (8093)"
 * - Method: POST
 * - URL: {{analysisUrl}}/internal/analysis
 * - Headers: Content-Type = application/json
 * - Body -> raw -> JSON (workId and fileId come from "Submit work"):
 *   {
 *     "workId": "{{workId}}",
 *     "fileId": "{{fileId}}",
 *     "studentFullName": "Ivan Ivanov",
 *     "originalFileName": "test.txt",
 *     "contentType": "text/plain",
 *     "fileSizeBytes": 20
 *   }
 * - Tests tab: block "Internal analysis".
 * - Note: this endpoint is not available through the gateway (by design).
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
// 9. GET {{analysisUrl}}/works/{{workId}}/reports - List reports (direct)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - GET {{analysisUrl}}/works/{{workId}}/reports
 * - Tests tab: block "Get reports (Gateway)" (same as step 2).
 */

// =============================================================================
// FOLDER: Negative scenarios (optional, gateway)
// =============================================================================

// -----------------------------------------------------------------------------
// 10. POST {{gatewayUrl}}/works - Invalid file (zip, blank name)
// -----------------------------------------------------------------------------
/*
 * Request:
 * - POST {{gatewayUrl}}/works
 * - form-data: studentFullName + postman/files/archive.zip
 *   (or a valid file with studentFullName set to spaces only)
 * - Tests tab: block "Invalid file upload".
 * - A file over 1 MiB may be rejected with 413 by the upload size limit instead of 400.
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
// 11. GET {{gatewayUrl}}/files/00000000-0000-0000-0000-000000000000 - Not found
// -----------------------------------------------------------------------------
/*
 * Request:
 * - GET {{gatewayUrl}}/files/00000000-0000-0000-0000-000000000000
 * - Tests tab: block "File not found".
 */
// --- File not found ---
pm.test("Status is 404 Not Found", function () {
    pm.response.to.have.status(404);
});
