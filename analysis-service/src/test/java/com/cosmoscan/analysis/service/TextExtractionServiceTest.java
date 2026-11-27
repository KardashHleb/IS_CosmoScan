package com.cosmoscan.analysis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TextExtractionServiceTest {

    private final TextExtractionService service = new TextExtractionService();

    @Test
    void extract_plainTextFile_returnsContent() throws Exception {
        String expected = "CosmoScan word cloud sample text";
        byte[] content = expected.getBytes(StandardCharsets.UTF_8);

        String extracted = service.extract(content, "sample.txt");

        assertThat(extracted).contains("CosmoScan");
        assertThat(extracted).contains("word cloud");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void extract_withoutFileName_detectsTypeFromContent(String fileName) throws Exception {
        byte[] content = "plain text without a file name".getBytes(StandardCharsets.UTF_8);

        assertThat(service.extract(content, fileName)).contains("plain text without a file name");
    }
}
