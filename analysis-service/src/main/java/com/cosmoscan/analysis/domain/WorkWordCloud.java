package com.cosmoscan.analysis.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "work_word_clouds")
public class WorkWordCloud {

    @Id
    private UUID id;

    @Column(name = "work_id", nullable = false, unique = true)
    private UUID workId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordCloudStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_word_cloud_terms", joinColumns = @JoinColumn(name = "word_cloud_id"))
    @OrderColumn(name = "term_order")
    private List<WordCloudTerm> terms = new ArrayList<>();

    protected WorkWordCloud() {
    }

    public WorkWordCloud(
            UUID id,
            UUID workId,
            UUID fileId,
            Instant generatedAt,
            WordCloudStatus status,
            List<WordCloudTerm> terms
    ) {
        this.id = id;
        this.workId = workId;
        this.fileId = fileId;
        this.generatedAt = generatedAt;
        this.status = status;
        this.terms = terms != null ? new ArrayList<>(terms) : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkId() {
        return workId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public WordCloudStatus getStatus() {
        return status;
    }

    public List<WordCloudTerm> getTerms() {
        return terms;
    }
}
