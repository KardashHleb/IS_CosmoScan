package com.cosmoscan.analysis.api.dto;

import com.cosmoscan.analysis.domain.ReportStatus;
import com.cosmoscan.analysis.domain.TechnicalReport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalReportResponseTest {

    @Test
    void from_mapsEntityFields() {
        UUID id = UUID.randomUUID();
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        Instant checkedAt = Instant.parse("2026-05-17T11:00:00Z");
        TechnicalReport entity = new TechnicalReport(
                id, workId, fileId, checkedAt, ReportStatus.ACCEPTED, List.of("ok"), "/tmp/r.json"
        );

        TechnicalReportResponse response = TechnicalReportResponse.from(entity);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.workId()).isEqualTo(workId);
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.checkedAt()).isEqualTo(checkedAt);
        assertThat(response.status()).isEqualTo(ReportStatus.ACCEPTED);
        assertThat(response.remarks()).containsExactly("ok");
    }
}
