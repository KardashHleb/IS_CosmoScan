package com.cosmoscan.submission.client;

import com.cosmoscan.submission.config.ServiceClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AnalysisClient {

    private final RestClient restClient;

    public AnalysisClient(ServiceClientProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getAnalysisBaseUrl())
                .build();
    }

    public TechnicalReportResponse analyze(AnalyzeWorkRequest request) {
        return restClient.post()
                .uri("/internal/analysis")
                .body(request)
                .retrieve()
                .body(TechnicalReportResponse.class);
    }
}
