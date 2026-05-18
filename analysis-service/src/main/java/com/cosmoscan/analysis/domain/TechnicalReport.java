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
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "technical_reports")
public class TechnicalReport {

    @Id
    private UUID id;

    @Column(name = "work_id", nullable = false)
    private UUID workId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technical_report_remarks", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "remark", nullable = false)
    private List<String> remarks = new ArrayList<>();

    @Column(name = "report_file_path")
    private String reportFilePath;

    protected TechnicalReport() {
    }

    public TechnicalReport(
            UUID id,
            UUID workId,
            UUID fileId,
            Instant checkedAt,
            ReportStatus status,
            List<String> remarks,
            String reportFilePath
    ) {
        this.id = id;
        this.workId = workId;
        this.fileId = fileId;
        this.checkedAt = checkedAt;
        this.status = status;
        this.remarks = remarks != null ? new ArrayList<>(remarks) : new ArrayList<>();
        this.reportFilePath = reportFilePath;
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

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public List<String> getRemarks() {
        return remarks;
    }

    public String getReportFilePath() {
        return reportFilePath;
    }
}
