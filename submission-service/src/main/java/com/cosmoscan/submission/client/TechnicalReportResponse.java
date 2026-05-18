package com.cosmoscan.submission.client;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TechnicalReportResponse(
        UUID id,
        UUID workId,
        UUID fileId,
        Instant checkedAt,
        String status,
        List<String> remarks
) {
}
