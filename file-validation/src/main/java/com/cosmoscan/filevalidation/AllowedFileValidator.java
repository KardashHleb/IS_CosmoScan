package com.cosmoscan.filevalidation;

import org.springframework.web.multipart.MultipartFile;

/**
 * Rejects files that break {@link FileRules} with an {@link IllegalArgumentException},
 * which every service maps to HTTP 400.
 */
public final class AllowedFileValidator {

    private AllowedFileValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        validate(file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    public static void validate(String originalFileName, String contentType, long fileSizeBytes) {
        if (FileRules.exceedsMaxSize(fileSizeBytes)) {
            throw new IllegalArgumentException(
                    "File size exceeds 1 MiB limit. Allowed maximum: " + FileRules.MAX_FILE_SIZE_BYTES + " bytes"
            );
        }

        String extension = FileRules.extractExtension(originalFileName);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Only files with extensions .pdf, .docx, .txt are allowed");
        }
        if (!FileRules.isAllowedExtension(extension)) {
            throw new IllegalArgumentException("Unsupported file format. Allowed: pdf, docx, txt");
        }

        if (FileRules.isZipContentType(contentType)) {
            throw new IllegalArgumentException("ZIP files are not allowed");
        }
    }
}
