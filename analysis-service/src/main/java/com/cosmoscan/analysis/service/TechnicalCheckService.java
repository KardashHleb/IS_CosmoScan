package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.domain.ReportStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TechnicalCheckService {

    private static final long MAX_FILE_SIZE_BYTES = 1_048_576L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt");

    public TechnicalCheckResult check(String originalFileName, String contentType, long fileSizeBytes) {
        List<String> remarks = new ArrayList<>();

        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            remarks.add("File size exceeds 1 MiB limit: " + fileSizeBytes + " bytes");
        }

        String extension = extractExtension(originalFileName);
        if (extension.isEmpty()) {
            remarks.add("File extension is missing");
        } else if ("zip".equals(extension)) {
            remarks.add("ZIP archives are not allowed");
        } else if (!ALLOWED_EXTENSIONS.contains(extension)) {
            remarks.add("Unsupported file format. Allowed: pdf, docx, txt");
        }

        if (contentType != null) {
            String lowerType = contentType.toLowerCase(Locale.ROOT);
            if (lowerType.contains("zip")) {
                remarks.add("ZIP content type is not allowed: " + contentType);
            }
        }

        ReportStatus status = remarks.isEmpty() ? ReportStatus.ACCEPTED : ReportStatus.NEEDS_REVISION;
        return new TechnicalCheckResult(status, List.copyOf(remarks));
    }

    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public record TechnicalCheckResult(ReportStatus status, List<String> remarks) {
    }
}
