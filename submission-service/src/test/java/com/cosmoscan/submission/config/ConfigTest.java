package com.cosmoscan.submission.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    @Test
    void openApi_describesServiceAndBothServers() {
        OpenAPI openAPI = new OpenApiConfig().openAPI(8091);

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CosmoScan - Submission Service");
        assertThat(openAPI.getServers()).extracting(Server::getUrl)
                .containsExactly("http://localhost:8091", "http://localhost:8090");
    }

    @Test
    void serviceClientProperties_haveLocalDefaultsAndCanBeOverridden() {
        ServiceClientProperties properties = new ServiceClientProperties();

        assertThat(properties.getFileStoringBaseUrl()).isEqualTo("http://localhost:8092");
        assertThat(properties.getAnalysisBaseUrl()).isEqualTo("http://localhost:8093");

        properties.setFileStoringBaseUrl("http://file-storing-service:8092");
        properties.setAnalysisBaseUrl("http://analysis-service:8093");

        assertThat(properties.getFileStoringBaseUrl()).isEqualTo("http://file-storing-service:8092");
        assertThat(properties.getAnalysisBaseUrl()).isEqualTo("http://analysis-service:8093");
    }
}
