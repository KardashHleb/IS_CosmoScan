package com.cosmoscan.filestoring.domain;

import java.time.Instant;
import java.util.UUID;

public record StoredFileMetadata(
        UUID fileId,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        String storagePath,
        Instant storedAt
) {
}
