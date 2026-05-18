package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.config.AnalysisProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class FileStoringClient {

    private final RestClient restClient;

    public FileStoringClient(AnalysisProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getFileStoringBaseUrl())
                .build();
    }

    public boolean fileExists(UUID fileId) {
        try {
            restClient.get()
                    .uri("/files/{fileId}", fileId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public byte[] download(UUID fileId) {
        byte[] content = restClient.get()
                .uri("/files/{fileId}", fileId)
                .retrieve()
                .body(byte[].class);
        if (content == null) {
            throw new IllegalStateException("Empty response body when downloading file: " + fileId);
        }
        return content;
    }
}
