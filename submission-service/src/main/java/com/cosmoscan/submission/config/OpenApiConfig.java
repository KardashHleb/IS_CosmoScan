package com.cosmoscan.submission.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${server.port:8091}") int serverPort) {
        return new OpenAPI()
                .info(new Info()
                        .title("CosmoScan — Submission Service")
                        .description("""
                                Accepts student work submissions (multipart), stores files via file-storing,
                                persists work metadata, and triggers technical analysis.

                                **Public API via gateway:** `POST http://localhost:8090/works`

                                **Swagger UI via gateway:** `http://localhost:8090/swagger/submission/swagger-ui/index.html`
                                """)
                        .version("1.0-SNAPSHOT"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct (this service)"),
                        new Server()
                                .url("http://localhost:8090")
                                .description("API Gateway")
                ));
    }
}
