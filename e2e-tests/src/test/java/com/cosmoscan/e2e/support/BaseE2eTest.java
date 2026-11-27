package com.cosmoscan.e2e.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import static io.restassured.RestAssured.given;

/**
 * Base class for end-to-end tests. All requests go through the API Gateway, exactly like a real
 * client. The gateway URL comes from the {@code cosmoscan.baseUrl} system property,
 * then the {@code COSMOSCAN_BASE_URL} environment variable, then {@code http://localhost:8090}.
 */
@Tag("e2e")
public abstract class BaseE2eTest {

    protected static final String BASE_URL = resolveBaseUrl();

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    protected static RequestSpecification api() {
        return given().spec(new RequestSpecBuilder().setBaseUri(BASE_URL).build());
    }

    private static String resolveBaseUrl() {
        String fromProperty = System.getProperty("cosmoscan.baseUrl");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv("COSMOSCAN_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return "http://localhost:8090";
    }
}
