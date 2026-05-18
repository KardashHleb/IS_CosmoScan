package com.cosmoscan.submission;

import com.cosmoscan.submission.config.ServiceClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServiceClientProperties.class)
public class SubmissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionServiceApplication.class, args);
    }
}
