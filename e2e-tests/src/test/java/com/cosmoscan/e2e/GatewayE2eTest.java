package com.cosmoscan.e2e;

import com.cosmoscan.e2e.support.BaseE2eTest;
import com.cosmoscan.e2e.support.TestFiles;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.cosmoscan.e2e.support.TestFiles.TXT_CONTENT_TYPE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/** Gateway health, Swagger routing and the fact that internal endpoints are not public. */
class GatewayE2eTest extends BaseE2eTest {

    @Test
    @DisplayName("Gateway health is UP")
    void gatewayHealthIsUp() {
        api().when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"submission", "files", "analysis"})
    @DisplayName("Swagger UI of every service is reachable through the gateway")
    void swaggerUiIsRoutedForEveryService(String service) {
        api().when()
                .get("/swagger/{service}/swagger-ui/index.html", service)
                .then()
                .statusCode(200)
                .body(containsString("Swagger UI"));
    }

    @Test
    @DisplayName("Internal analysis endpoint is not exposed")
    void internalAnalysisIsNotExposed() {
        api()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/internal/analysis")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Stored file cannot be deleted through the gateway")
    void storedFileCannotBeDeletedThroughGateway() {
        String fileId = api()
                .multiPart("file", "keep-me.txt", TestFiles.text("keep me"), TXT_CONTENT_TYPE)
                .when()
                .post("/files")
                .then()
                .statusCode(201)
                .extract().path("fileId");

        api().when().delete("/internal/files/{fileId}", fileId).then().statusCode(404);
        api().when().delete("/files/{fileId}", fileId).then().statusCode(greaterThanOrEqualTo(400));

        api().when()
                .get("/files/{fileId}", fileId)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Unknown route is 404")
    void unknownRouteIsNotFound() {
        api().when()
                .get("/unknown/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }
}
