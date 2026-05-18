package com.cosmoscan.filestoring;

import com.cosmoscan.filestoring.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class FileStoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStoringServiceApplication.class, args);
    }
}
