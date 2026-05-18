package com.cosmoscan.analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cosmoscan.analysis")
public class AnalysisProperties {

    private String fileStoringBaseUrl = "http://localhost:8092";
    private String reportsPath = "./storage/reports";

    public String getFileStoringBaseUrl() {
        return fileStoringBaseUrl;
    }

    public void setFileStoringBaseUrl(String fileStoringBaseUrl) {
        this.fileStoringBaseUrl = fileStoringBaseUrl;
    }

    public String getReportsPath() {
        return reportsPath;
    }

    public void setReportsPath(String reportsPath) {
        this.reportsPath = reportsPath;
    }
}
