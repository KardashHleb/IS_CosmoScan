package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.config.AnalysisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class ReportFileWriter {

    private final Path reportsRoot;
    private final ObjectMapper objectMapper;

    public ReportFileWriter(AnalysisProperties properties, ObjectMapper objectMapper) throws IOException {
        this.reportsRoot = Path.of(properties.getReportsPath()).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        Files.createDirectories(this.reportsRoot);
    }

    public String write(UUID reportId, TechnicalReportResponse report) throws IOException {
        Path target = reportsRoot.resolve(reportId + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), report);
        return target.toString();
    }
}
