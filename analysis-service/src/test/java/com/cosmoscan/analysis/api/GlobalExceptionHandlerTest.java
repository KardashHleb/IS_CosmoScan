package com.cosmoscan.analysis.api;

import com.cosmoscan.analysis.exception.WordCloudNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ProblemDetail detail = handler.handleIllegalArgument(new IllegalArgumentException("invalid"));

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getDetail()).isEqualTo("invalid");
    }

    @Test
    void handleWordCloudNotFound_returnsNotFound() {
        UUID workId = UUID.randomUUID();
        ProblemDetail detail = handler.handleWordCloudNotFound(new WordCloudNotFoundException(workId));

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getDetail()).contains(workId.toString());
    }
}
