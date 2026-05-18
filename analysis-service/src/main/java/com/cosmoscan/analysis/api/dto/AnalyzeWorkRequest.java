package com.cosmoscan.analysis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record AnalyzeWorkRequest(
        @NotNull UUID workId,
        @NotNull UUID fileId,
        @NotBlank String studentFullName,
        @NotBlank String originalFileName,
        String contentType,
        @PositiveOrZero long fileSizeBytes
) {
}
