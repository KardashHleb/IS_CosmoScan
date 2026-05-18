package com.cosmoscan.submission.client;

import java.time.Instant;
import java.util.UUID;

public record StoredFileResponse(
        UUID fileId,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        Instant storedAt
) {
}
