package com.cosmoscan.analysis.config;

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
    public OpenAPI openAPI(@Value("${server.port:8093}") int serverPort) {
        return new OpenAPI()
                .info(new Info()
                        .title("CosmoScan — Analysis Service")
                        .description("""
                                Runs technical checks on submitted works and stores technical reports.

                                **Public API via gateway:** `GET http://localhost:8090/works/{workId}/reports`

                                **Internal API (submission only, not routed via gateway):**
                                `POST /internal/analysis` on this service port.

                                **Swagger UI via gateway:** `http://localhost:8090/swagger/analysis/swagger-ui/index.html`
                                """)
                        .version("1.0-SNAPSHOT"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct (this service)"),
                        new Server()
                                .url("http://localhost:8090")
                                .description("API Gateway (GET reports only)")
                ));
    }
}
