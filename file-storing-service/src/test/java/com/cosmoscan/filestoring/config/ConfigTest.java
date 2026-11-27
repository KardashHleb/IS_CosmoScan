package com.cosmoscan.filestoring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    @Test
    void openApi_describesServiceAndBothServers() {
        OpenAPI openAPI = new OpenApiConfig().openAPI(8092);

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CosmoScan - File Storing Service");
        assertThat(openAPI.getServers()).extracting(Server::getUrl)
                .containsExactly("http://localhost:8092", "http://localhost:8090");
    }

    @Test
    void storageProperties_haveLocalDefault() {
        assertThat(new StorageProperties().getFilesPath()).isEqualTo("./storage/files");
    }
}
