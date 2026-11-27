package com.cosmoscan.e2e;

import com.cosmoscan.e2e.support.BaseE2eTest;
import com.cosmoscan.e2e.support.TestFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.cosmoscan.e2e.support.TestFiles.TXT_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/** UC-4: direct upload and download through the gateway, without a work. */
class FileStorageE2eTest extends BaseE2eTest {

    @Test
    @DisplayName("Uploaded file can be downloaded as an attachment with the same content")
    void uploadedFileCanBeDownloaded() {
        byte[] content = TestFiles.fixture("cosmos-notes.txt");

        String fileId = api()
                .multiPart("file", "cosmos-notes.txt", content, TXT_CONTENT_TYPE)
                .when()
                .post("/files")
                .then()
                .statusCode(201)
                .body("fileId", notNullValue())
                .body("originalFileName", equalTo("cosmos-notes.txt"))
                .body("contentType", equalTo(TXT_CONTENT_TYPE))
                .body("fileSizeBytes", equalTo(content.length))
                .extract().path("fileId");

        byte[] downloaded = api().when()
                .get("/files/{fileId}", fileId)
                .then()
                .statusCode(200)
                .header("Content-Disposition", containsString("attachment"))
                .header("Content-Disposition", containsString("cosmos-notes.txt"))
                .contentType(containsString(TXT_CONTENT_TYPE))
                .extract().asByteArray();

        assertThat(downloaded).isEqualTo(content);
    }

    @Test
    @DisplayName("Direct upload applies the same file rules")
    void directUploadRejectsDisallowedFile() {
        api()
                .multiPart("file", "image.png", TestFiles.text("png"), "image/png")
                .when()
                .post("/files")
                .then()
                .statusCode(400)
                .body("detail", containsString("Unsupported file format"));
    }

    @Test
    @DisplayName("Unknown file id is 404")
    void unknownFileIsNotFound() {
        api().when()
                .get("/files/{fileId}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }
}
