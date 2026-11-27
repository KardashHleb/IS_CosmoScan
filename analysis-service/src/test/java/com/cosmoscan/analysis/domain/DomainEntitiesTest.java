package com.cosmoscan.analysis.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntitiesTest {

    private static final Instant NOW = Instant.parse("2026-05-17T10:00:00Z");

    @Test
    void technicalReport_exposesAllFieldsAndCopiesRemarks() {
        UUID id = UUID.randomUUID();
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        List<String> remarks = List.of("File extension is missing");

        TechnicalReport report = new TechnicalReport(
                id, workId, fileId, NOW, ReportStatus.NEEDS_REVISION, remarks, "/reports/" + id + ".json");

        assertThat(report.getId()).isEqualTo(id);
        assertThat(report.getWorkId()).isEqualTo(workId);
        assertThat(report.getFileId()).isEqualTo(fileId);
        assertThat(report.getCheckedAt()).isEqualTo(NOW);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(report.getRemarks()).containsExactly("File extension is missing").isNotSameAs(remarks);
        assertThat(report.getReportFilePath()).isEqualTo("/reports/" + id + ".json");
    }

    @Test
    void technicalReport_nullRemarks_becomeEmptyList() {
        TechnicalReport report = new TechnicalReport(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW, ReportStatus.ACCEPTED, null, null);

        assertThat(report.getRemarks()).isEmpty();
    }

    @Test
    void workWordCloud_exposesAllFieldsAndCopiesTerms() {
        UUID id = UUID.randomUUID();
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        List<WordCloudTerm> terms = List.of(new WordCloudTerm("orbit", 3));

        WorkWordCloud cloud = new WorkWordCloud(id, workId, fileId, NOW, WordCloudStatus.READY, terms);

        assertThat(cloud.getId()).isEqualTo(id);
        assertThat(cloud.getWorkId()).isEqualTo(workId);
        assertThat(cloud.getFileId()).isEqualTo(fileId);
        assertThat(cloud.getGeneratedAt()).isEqualTo(NOW);
        assertThat(cloud.getStatus()).isEqualTo(WordCloudStatus.READY);
        assertThat(cloud.getTerms()).isNotSameAs(terms).singleElement()
                .satisfies(term -> {
                    assertThat(term.getText()).isEqualTo("orbit");
                    assertThat(term.getWeight()).isEqualTo(3);
                });
    }

    @Test
    void workWordCloud_nullTerms_becomeEmptyList() {
        WorkWordCloud cloud = new WorkWordCloud(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW, WordCloudStatus.NO_TEXT, null);

        assertThat(cloud.getTerms()).isEmpty();
    }

    @Test
    void jpaConstructors_createEmptyEntities() {
        assertThat(new TechnicalReport().getId()).isNull();
        assertThat(new WorkWordCloud().getId()).isNull();
        assertThat(new WordCloudTerm().getText()).isNull();
    }
}
