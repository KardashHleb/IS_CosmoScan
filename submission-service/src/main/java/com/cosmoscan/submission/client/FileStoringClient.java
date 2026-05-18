package com.cosmoscan.submission.client;

import com.cosmoscan.submission.config.ServiceClientProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileStoringClient {

    private final RestClient restClient;

    public FileStoringClient(ServiceClientProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getFileStoringBaseUrl())
                .build();
    }

    public StoredFileResponse store(MultipartFile file) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartData = builder.build();

        return restClient.post()
                .uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipartData)
                .retrieve()
                .body(StoredFileResponse.class);
    }
}
