package com.cosmoscan.analysis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    @Test
    void openApi_describesServiceAndBothServers() {
        OpenAPI openAPI = new OpenApiConfig().openAPI(8093);

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CosmoScan - Analysis Service");
        assertThat(openAPI.getServers()).extracting(Server::getUrl)
                .containsExactly("http://localhost:8093", "http://localhost:8090");
    }

    @Test
    void analysisProperties_haveLocalDefaults() {
        AnalysisProperties properties = new AnalysisProperties();

        assertThat(properties.getFileStoringBaseUrl()).isEqualTo("http://localhost:8092");
        assertThat(properties.getReportsPath()).isEqualTo("./storage/reports");

        properties.setFileStoringBaseUrl("http://file-storing-service:8092");
        properties.setReportsPath("/data/reports");

        assertThat(properties.getFileStoringBaseUrl()).isEqualTo("http://file-storing-service:8092");
        assertThat(properties.getReportsPath()).isEqualTo("/data/reports");
    }
}
