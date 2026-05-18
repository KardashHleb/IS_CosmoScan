package com.cosmoscan.submission.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpClientErrorException.BadRequest badRequestException;

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ProblemDetail detail = handler.handleIllegalArgument(new IllegalArgumentException("bad file"));

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getDetail()).isEqualTo("bad file");
    }

    @Test
    void handleBadRequestFromDownstream_usesResponseBody() {
        when(badRequestException.getResponseBodyAsString()).thenReturn("{\"detail\":\"Invalid file\"}");

        ProblemDetail detail = handler.handleBadRequestFromDownstream(badRequestException);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getDetail()).contains("Invalid file");
    }

    @Test
    void handleBadRequestFromDownstream_blankBodyUsesDefaultMessage() {
        when(badRequestException.getResponseBodyAsString()).thenReturn("  ");

        ProblemDetail detail = handler.handleBadRequestFromDownstream(badRequestException);

        assertThat(detail.getDetail()).isEqualTo("Invalid file");
    }
}
