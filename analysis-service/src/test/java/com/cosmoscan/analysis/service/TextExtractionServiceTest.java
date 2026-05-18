package com.cosmoscan.analysis.service;

import org.junit.jupiter.api.Test;

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
}
