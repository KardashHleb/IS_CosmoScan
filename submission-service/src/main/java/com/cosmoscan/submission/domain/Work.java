package com.cosmoscan.submission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "works")
public class Work {

    @Id
    private UUID id;

    @Column(name = "student_full_name", nullable = false)
    private String studentFullName;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    protected Work() {
    }

    public Work(
            UUID id,
            String studentFullName,
            Instant submittedAt,
            String originalFileName,
            UUID fileId,
            String contentType,
            long fileSizeBytes
    ) {
        this.id = id;
        this.studentFullName = studentFullName;
        this.submittedAt = submittedAt;
        this.originalFileName = originalFileName;
        this.fileId = fileId;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
    }

    public UUID getId() {
        return id;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
}
