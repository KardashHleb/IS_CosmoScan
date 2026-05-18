package com.cosmoscan.submission.api.dto;

import com.cosmoscan.submission.client.TechnicalReportResponse;
import com.cosmoscan.submission.domain.Work;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkResponseTest {

    @Test
    void from_mapsWorkAndReport() {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Work work = new Work(
                workId,
                "Anna",
                Instant.parse("2026-05-17T09:00:00Z"),
                "paper.pdf",
                fileId,
                "application/pdf",
                1024
        );
        TechnicalReportResponse report = new TechnicalReportResponse(
                reportId, workId, fileId, Instant.parse("2026-05-17T09:01:00Z"),
                "NEEDS_REVISION", List.of("too large")
        );

        WorkResponse response = WorkResponse.from(work, report);

        assertThat(response.id()).isEqualTo(workId);
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.technicalReport().status()).isEqualTo("NEEDS_REVISION");
        assertThat(response.technicalReport().remarks()).containsExactly("too large");
    }
}
