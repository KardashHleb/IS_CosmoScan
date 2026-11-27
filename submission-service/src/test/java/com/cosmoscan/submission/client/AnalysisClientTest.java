package com.cosmoscan.submission.client;

import com.cosmoscan.submission.config.ServiceClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AnalysisClientTest {

    private static final String BASE_URL = "http://analysis.test";

    @Test
    void analyze_postsRequestToInternalEndpointAndReturnsReport() {
        ServiceClientProperties properties = new ServiceClientProperties();
        properties.setAnalysisBaseUrl(BASE_URL);
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AnalysisClient client = new AnalysisClient(properties, builder);

        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        server.expect(requestTo(BASE_URL + "/internal/analysis"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.workId").value(workId.toString()))
                .andExpect(jsonPath("$.originalFileName").value("work.pdf"))
                .andRespond(withSuccess("""
                        {"id":"%s","workId":"%s","fileId":"%s","checkedAt":"2026-05-17T10:00:01Z",
                         "status":"NEEDS_REVISION","remarks":["File size exceeds 1 MiB limit"]}
                        """.formatted(reportId, workId, fileId), MediaType.APPLICATION_JSON));

        TechnicalReportResponse report = client.analyze(new AnalyzeWorkRequest(
                workId, fileId, "Ivan Ivanov", "work.pdf", "application/pdf", 2_000_000));

        server.verify();
        assertThat(report.id()).isEqualTo(reportId);
        assertThat(report.workId()).isEqualTo(workId);
        assertThat(report.status()).isEqualTo("NEEDS_REVISION");
        assertThat(report.remarks()).containsExactly("File size exceeds 1 MiB limit");
    }
}
