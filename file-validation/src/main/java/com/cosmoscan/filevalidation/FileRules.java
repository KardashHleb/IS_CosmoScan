package com.cosmoscan.filevalidation;

import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for the file rules shared by submission-service, file-storing-service
 * and analysis-service: allowed extensions, maximum size and the ZIP ban.
 */
public final class FileRules {

    public static final long MAX_FILE_SIZE_BYTES = 1_048_576L;
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt");

    private FileRules() {
    }

    /** Lower-case extension without the dot, or an empty string when there is none. */
    public static String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public static boolean isAllowedExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    public static boolean exceedsMaxSize(long fileSizeBytes) {
        return fileSizeBytes > MAX_FILE_SIZE_BYTES;
    }

    public static boolean isZipContentType(String contentType) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("zip");
    }
}
