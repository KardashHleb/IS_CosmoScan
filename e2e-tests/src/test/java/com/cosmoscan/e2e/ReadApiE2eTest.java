package com.cosmoscan.e2e;

import com.cosmoscan.e2e.support.BaseE2eTest;
import com.cosmoscan.e2e.support.TestFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.cosmoscan.e2e.support.TestFiles.TXT_CONTENT_TYPE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

/** Read endpoints for works that do not exist, and word cloud edge cases. */
class ReadApiE2eTest extends BaseE2eTest {

    @Test
    @DisplayName("Reports of an unknown work are an empty list")
    void reportsOfUnknownWorkAreEmpty() {
        api().when()
                .get("/works/{workId}/reports", UUID.randomUUID())
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("Word cloud of an unknown work is 404")
    void wordCloudOfUnknownWorkIsNotFound() {
        api().when()
                .get("/works/{workId}/word-cloud", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Malformed work id is 400")
    void malformedWorkIdIsBadRequest() {
        api().when()
                .get("/works/{workId}/reports", "not-a-uuid")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Text of only stop words and short words gives a NO_TEXT word cloud")
    void stopWordsOnlyGiveNoTextWordCloud() {
        String workId = api()
                .multiPart("studentFullName", "Ivan Ivanov")
                .multiPart("file", "stop-words.txt", TestFiles.text("the and of to it is a an"), TXT_CONTENT_TYPE)
                .when()
                .post("/works")
                .then()
                .statusCode(201)
                .extract().path("id");

        api().when()
                .get("/works/{workId}/word-cloud", workId)
                .then()
                .statusCode(200)
                .body("status", equalTo("NO_TEXT"))
                .body("terms", empty());
    }
}
