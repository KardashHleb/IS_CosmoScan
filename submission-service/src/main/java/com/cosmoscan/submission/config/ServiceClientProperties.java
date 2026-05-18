package com.cosmoscan.submission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cosmoscan.clients")
public class ServiceClientProperties {

    private String fileStoringBaseUrl = "http://localhost:8092";
    private String analysisBaseUrl = "http://localhost:8093";

    public String getFileStoringBaseUrl() {
        return fileStoringBaseUrl;
    }

    public void setFileStoringBaseUrl(String fileStoringBaseUrl) {
        this.fileStoringBaseUrl = fileStoringBaseUrl;
    }

    public String getAnalysisBaseUrl() {
        return analysisBaseUrl;
    }

    public void setAnalysisBaseUrl(String analysisBaseUrl) {
        this.analysisBaseUrl = analysisBaseUrl;
    }
}
