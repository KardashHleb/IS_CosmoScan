package com.cosmoscan.filestoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cosmoscan.storage")
public class StorageProperties {

    private String filesPath = "./storage/files";

    public String getFilesPath() {
        return filesPath;
    }

    public void setFilesPath(String filesPath) {
        this.filesPath = filesPath;
    }
}
