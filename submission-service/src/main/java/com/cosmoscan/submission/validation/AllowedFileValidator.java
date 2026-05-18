package com.cosmoscan.submission.validation;

import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

public final class AllowedFileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 1_048_576L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt");

    private AllowedFileValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        validate(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "",
                file.getContentType(),
                file.getSize()
        );
    }

    public static void validate(String originalFileName, String contentType, long fileSizeBytes) {
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds 1 MiB limit. Allowed maximum: " + MAX_FILE_SIZE_BYTES + " bytes"
            );
        }

        String extension = extractExtension(originalFileName);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Only files with extensions .pdf, .docx, .txt are allowed");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file format. Allowed: pdf, docx, txt");
        }

        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("zip")) {
            throw new IllegalArgumentException("ZIP files are not allowed");
        }
    }

    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
