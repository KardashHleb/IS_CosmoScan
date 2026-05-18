package com.cosmoscan.submission.client;

import java.util.UUID;

public record AnalyzeWorkRequest(
        UUID workId,
        UUID fileId,
        String studentFullName,
        String originalFileName,
        String contentType,
        long fileSizeBytes
) {
}
