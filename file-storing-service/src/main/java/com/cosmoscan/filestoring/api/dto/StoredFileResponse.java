package com.cosmoscan.filestoring.api.dto;

import com.cosmoscan.filestoring.domain.StoredFileMetadata;

import java.time.Instant;
import java.util.UUID;

public record StoredFileResponse(
        UUID fileId,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        Instant storedAt
) {

    public static StoredFileResponse from(StoredFileMetadata metadata) {
        return new StoredFileResponse(
                metadata.fileId(),
                metadata.originalFileName(),
                metadata.contentType(),
                metadata.fileSizeBytes(),
                metadata.storedAt()
        );
    }
}
