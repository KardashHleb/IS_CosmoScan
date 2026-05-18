package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.config.AnalysisProperties;
import com.cosmoscan.analysis.domain.ReportStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void write_persistsReportAsJsonFile() throws Exception {
        AnalysisProperties properties = new AnalysisProperties();
        properties.setReportsPath(tempDir.toString());

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ReportFileWriter writer = new ReportFileWriter(properties, objectMapper);
        UUID reportId = UUID.randomUUID();
        TechnicalReportResponse report = new TechnicalReportResponse(
                reportId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-05-17T12:00:00Z"),
                ReportStatus.ACCEPTED,
                List.of()
        );

        String path = writer.write(reportId, report);

        assertThat(Files.exists(Path.of(path))).isTrue();
        assertThat(Files.readString(Path.of(path))).contains("ACCEPTED");
    }
}
