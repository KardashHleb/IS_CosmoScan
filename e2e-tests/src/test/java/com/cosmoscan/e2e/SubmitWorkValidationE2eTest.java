package com.cosmoscan.e2e;

import com.cosmoscan.e2e.support.BaseE2eTest;
import com.cosmoscan.e2e.support.TestFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.cosmoscan.e2e.support.TestFiles.MAX_FILE_SIZE_BYTES;
import static com.cosmoscan.e2e.support.TestFiles.TXT_CONTENT_TYPE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/** POST /works rejects files and requests that break the upload rules. */
class SubmitWorkValidationE2eTest extends BaseE2eTest {

    static Stream<Arguments> rejectedFiles() {
        return Stream.of(
                Arguments.of("unsupported extension", "image.png", "image/png", TestFiles.text("png"),
                        "Unsupported file format"),
                Arguments.of("no extension", "README", TXT_CONTENT_TYPE, TestFiles.text("text"),
                        "Only files with extensions"),
                Arguments.of("zip content type", "work.txt", "application/zip", TestFiles.text("PK"),
                        "ZIP files are not allowed"),
                Arguments.of("zip extension", "work.zip", "application/octet-stream", TestFiles.text("PK"),
                        "Unsupported file format"),
                Arguments.of("empty file", "work.txt", TXT_CONTENT_TYPE, new byte[0],
                        "File must not be empty")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedFiles")
    @DisplayName("Disallowed file is rejected with 400 and a reason")
    void disallowedFileIsRejected(String caseName, String fileName, String contentType, byte[] content,
                                  String expectedDetail) {
        api()
                .multiPart("studentFullName", "Ivan Ivanov")
                .multiPart("file", fileName, content, contentType)
                .when()
                .post("/works")
                .then()
                .statusCode(400)
                .body("detail", containsString(expectedDetail));
    }

    @Test
    @DisplayName("File of exactly 1 MiB is accepted")
    void fileAtSizeLimitIsAccepted() {
        api()
                .multiPart("studentFullName", "Ivan Ivanov")
                .multiPart("file", "limit.txt", TestFiles.ofSize(MAX_FILE_SIZE_BYTES), TXT_CONTENT_TYPE)
                .when()
                .post("/works")
                .then()
                .statusCode(201)
                .body("technicalReport.status", equalTo("ACCEPTED"));
    }

    @Test
    @DisplayName("File one byte over 1 MiB is rejected")
    void fileOverSizeLimitIsRejected() {
        // Rejected either by the servlet multipart limit (413) or by the validator (400)
        api()
                .multiPart("studentFullName", "Ivan Ivanov")
                .multiPart("file", "too-big.txt", TestFiles.ofSize(MAX_FILE_SIZE_BYTES + 1), TXT_CONTENT_TYPE)
                .when()
                .post("/works")
                .then()
                .statusCode(anyOf(is(400), is(413)));
    }

    @Test
    @DisplayName("Blank student name is rejected with 400")
    void blankStudentNameIsRejected() {
        api()
                .multiPart("studentFullName", "   ")
                .multiPart("file", "work.txt", TestFiles.text("text"), TXT_CONTENT_TYPE)
                .when()
                .post("/works")
                .then()
                .statusCode(400)
                .body("detail", containsString("studentFullName"));
    }

    @Test
    @DisplayName("Missing student name is rejected with 400")
    void missingStudentNameIsRejected() {
        api()
                .multiPart("file", "work.txt", TestFiles.text("text"), TXT_CONTENT_TYPE)
                .when()
                .post("/works")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Missing file is rejected with 400")
    void missingFileIsRejected() {
        api()
                .multiPart("studentFullName", "Ivan Ivanov")
                .when()
                .post("/works")
                .then()
                .statusCode(400);
    }
}
