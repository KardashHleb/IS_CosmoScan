package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.config.AnalysisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FileStoringClientTest {

    private static final String BASE_URL = "http://file-storing.test";

    private MockRestServiceServer server;
    private FileStoringClient client;

    @BeforeEach
    void setUp() {
        AnalysisProperties properties = new AnalysisProperties();
        properties.setFileStoringBaseUrl(BASE_URL);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new FileStoringClient(properties, builder);
    }

    @Test
    void fileExists_whenFileStoringReturnsOk_isTrue() {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/files/" + fileId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("content", MediaType.TEXT_PLAIN));

        assertThat(client.fileExists(fileId)).isTrue();
        server.verify();
    }

    @Test
    void fileExists_whenFileStoringReturnsNotFound_isFalse() {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/files/" + fileId))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.fileExists(fileId)).isFalse();
    }

    @Test
    void download_returnsFileBytes() {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/files/" + fileId))
                .andRespond(withSuccess("content", MediaType.APPLICATION_OCTET_STREAM));

        assertThat(client.download(fileId)).isEqualTo("content".getBytes());
    }

    @Test
    void download_emptyBody_throws() {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/files/" + fileId))
                .andRespond(withStatus(HttpStatus.OK));

        assertThatThrownBy(() -> client.download(fileId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(fileId.toString());
    }
}
