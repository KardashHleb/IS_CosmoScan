package com.cosmoscan.analysis.api.dto;

import com.cosmoscan.analysis.domain.ReportStatus;
import com.cosmoscan.analysis.domain.TechnicalReport;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TechnicalReportResponse(
        UUID id,
        UUID workId,
        UUID fileId,
        Instant checkedAt,
        ReportStatus status,
        List<String> remarks
) {

    public static TechnicalReportResponse from(TechnicalReport report) {
        return new TechnicalReportResponse(
                report.getId(),
                report.getWorkId(),
                report.getFileId(),
                report.getCheckedAt(),
                report.getStatus(),
                List.copyOf(report.getRemarks())
        );
    }
}
