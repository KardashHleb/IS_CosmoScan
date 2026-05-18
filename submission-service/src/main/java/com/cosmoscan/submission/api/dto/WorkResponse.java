package com.cosmoscan.submission.api.dto;

import com.cosmoscan.submission.client.TechnicalReportResponse;
import com.cosmoscan.submission.domain.Work;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkResponse(
        UUID id,
        String studentFullName,
        Instant submittedAt,
        String originalFileName,
        UUID fileId,
        long fileSizeBytes,
        String contentType,
        TechnicalReportSummary technicalReport
) {

    public static WorkResponse from(Work work, TechnicalReportResponse report) {
        return new WorkResponse(
                work.getId(),
                work.getStudentFullName(),
                work.getSubmittedAt(),
                work.getOriginalFileName(),
                work.getFileId(),
                work.getFileSizeBytes(),
                work.getContentType(),
                TechnicalReportSummary.from(report)
        );
    }

    public record TechnicalReportSummary(
            UUID id,
            String status,
            List<String> remarks,
            Instant checkedAt
    ) {

        public static TechnicalReportSummary from(TechnicalReportResponse report) {
            return new TechnicalReportSummary(
                    report.id(),
                    report.status(),
                    report.remarks(),
                    report.checkedAt()
            );
        }
    }
}
