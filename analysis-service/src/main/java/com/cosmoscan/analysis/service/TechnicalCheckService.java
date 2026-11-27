package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.domain.ReportStatus;
import com.cosmoscan.filevalidation.FileRules;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks file metadata against {@link FileRules}. Unlike the upload validator it does not
 * reject the file: every broken rule becomes a remark and the report gets NEEDS_REVISION.
 */
@Service
public class TechnicalCheckService {

    public TechnicalCheckResult check(String originalFileName, String contentType, long fileSizeBytes) {
        List<String> remarks = new ArrayList<>();

        if (FileRules.exceedsMaxSize(fileSizeBytes)) {
            remarks.add("File size exceeds 1 MiB limit: " + fileSizeBytes + " bytes");
        }

        String extension = FileRules.extractExtension(originalFileName);
        if (extension.isEmpty()) {
            remarks.add("File extension is missing");
        } else if ("zip".equals(extension)) {
            remarks.add("ZIP archives are not allowed");
        } else if (!FileRules.isAllowedExtension(extension)) {
            remarks.add("Unsupported file format. Allowed: pdf, docx, txt");
        }

        if (FileRules.isZipContentType(contentType)) {
            remarks.add("ZIP content type is not allowed: " + contentType);
        }

        ReportStatus status = remarks.isEmpty() ? ReportStatus.ACCEPTED : ReportStatus.NEEDS_REVISION;
        return new TechnicalCheckResult(status, List.copyOf(remarks));
    }

    public record TechnicalCheckResult(ReportStatus status, List<String> remarks) {
    }
}
