package com.cosmoscan.e2e;

import com.cosmoscan.e2e.support.BaseE2eTest;
import com.cosmoscan.e2e.support.TestFiles;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.cosmoscan.e2e.support.TestFiles.DOCX_CONTENT_TYPE;
import static com.cosmoscan.e2e.support.TestFiles.PDF_CONTENT_TYPE;
import static com.cosmoscan.e2e.support.TestFiles.TXT_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * UC-1 + UC-2 + UC-3 in one flow: a student submits a work, then the report history,
 * the word cloud and the stored file are read back through the gateway.
 */
class SubmitWorkE2eTest extends BaseE2eTest {

    static Stream<Arguments> supportedFiles() {
        return Stream.of(
                Arguments.of("cosmos-notes.txt", TXT_CONTENT_TYPE, "cosmos"),
                Arguments.of("satellite-report.docx", DOCX_CONTENT_TYPE, "satellite"),
                Arguments.of("telescope-log.pdf", PDF_CONTENT_TYPE, "telescope")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedFiles")
    @DisplayName("Submitted work is accepted, reported, visualized and downloadable")
    void submittedWorkIsAcceptedAndAvailableThroughAllReadEndpoints(
            String fileName, String contentType, String dominantWord) {
        byte[] content = TestFiles.fixture(fileName);

        // UC-1: submit
        JsonPath work = api()
                .multiPart("studentFullName", "  Ivan Ivanov  ")
                .multiPart("file", fileName, content, contentType)
                .when()
                .post("/works")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("fileId", notNullValue())
                .body("studentFullName", equalTo("Ivan Ivanov"))
                .body("originalFileName", equalTo(fileName))
                .body("fileSizeBytes", equalTo(content.length))
                .body("technicalReport.status", equalTo("ACCEPTED"))
                .body("technicalReport.remarks", empty())
                .extract().jsonPath();
        String workId = work.getString("id");
        String fileId = work.getString("fileId");
        String reportId = work.getString("technicalReport.id");

        // UC-2: report history contains exactly the report returned on submit
        api().when()
                .get("/works/{workId}/reports", workId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(reportId))
                .body("[0].workId", equalTo(workId))
                .body("[0].fileId", equalTo(fileId))
                .body("[0].status", equalTo("ACCEPTED"));

        // UC-3: word cloud is built from the file text, the dominant word comes first
        api().when()
                .get("/works/{workId}/word-cloud", workId)
                .then()
                .statusCode(200)
                .body("workId", equalTo(workId))
                .body("status", equalTo("READY"))
                .body("terms[0].text", equalTo(dominantWord))
                .body("terms[0].weight", equalTo(4));

        // Stored file is the same bytes that were submitted
        byte[] downloaded = api().when()
                .get("/files/{fileId}", fileId)
                .then()
                .statusCode(200)
                .extract().asByteArray();
        assertThat(downloaded).isEqualTo(content);
    }
}
