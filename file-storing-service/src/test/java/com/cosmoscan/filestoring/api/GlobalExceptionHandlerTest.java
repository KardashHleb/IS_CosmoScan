package com.cosmoscan.filestoring.api;

import com.cosmoscan.filestoring.service.FileStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ProblemDetail detail = handler.handleIllegalArgument(new IllegalArgumentException("bad"));

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleNotFound_returns404() {
        ProblemDetail detail = handler.handleNotFound(
                new FileStoreService.FileNotFoundException("missing")
        );

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getDetail()).isEqualTo("missing");
    }
}
