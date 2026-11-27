package com.cosmoscan.submission.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTest {

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        Instant submittedAt = Instant.parse("2026-05-17T10:00:00Z");

        Work work = new Work(id, "Ivan Ivanov", submittedAt, "essay.txt", fileId, "text/plain", 5);

        assertThat(work.getId()).isEqualTo(id);
        assertThat(work.getStudentFullName()).isEqualTo("Ivan Ivanov");
        assertThat(work.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(work.getOriginalFileName()).isEqualTo("essay.txt");
        assertThat(work.getFileId()).isEqualTo(fileId);
        assertThat(work.getContentType()).isEqualTo("text/plain");
        assertThat(work.getFileSizeBytes()).isEqualTo(5);
    }

    @Test
    void jpaConstructor_createsEmptyEntity() {
        Work work = new Work();

        assertThat(work.getId()).isNull();
        assertThat(work.getFileSizeBytes()).isZero();
    }
}
