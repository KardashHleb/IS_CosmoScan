package com.cosmoscan.submission.client;

import com.cosmoscan.submission.config.ServiceClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FileStoringClientTest {

    private static final String BASE_URL = "http://file-storing.test";

    private MockRestServiceServer server;
    private FileStoringClient client;

    @BeforeEach
    void setUp() {
        ServiceClientProperties properties = new ServiceClientProperties();
        properties.setFileStoringBaseUrl(BASE_URL);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new FileStoringClient(properties, builder);
    }

    @Test
    void store_postsMultipartFileAndReturnsMetadata() throws Exception {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/files"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)))
                .andRespond(withSuccess("""
                        {"fileId":"%s","originalFileName":"work.txt","contentType":"text/plain",
                         "fileSizeBytes":5,"storedAt":"2026-05-17T10:00:00Z"}
                        """.formatted(fileId), MediaType.APPLICATION_JSON));

        StoredFileResponse response = client.store(
                new MockMultipartFile("file", "work.txt", "text/plain", "hello".getBytes()));

        server.verify();
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.originalFileName()).isEqualTo("work.txt");
        assertThat(response.contentType()).isEqualTo("text/plain");
        assertThat(response.fileSizeBytes()).isEqualTo(5);
        assertThat(response.storedAt()).isEqualTo(Instant.parse("2026-05-17T10:00:00Z"));
    }

    @Test
    void delete_callsInternalDeleteEndpoint() {
        UUID fileId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/internal/files/" + fileId))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        client.delete(fileId);

        server.verify();
    }
}
