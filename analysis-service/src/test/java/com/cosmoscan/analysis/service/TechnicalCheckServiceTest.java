package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.domain.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalCheckServiceTest {

    private final TechnicalCheckService service = new TechnicalCheckService();

    @Test
    void check_validTxtFile_isAccepted() {
        var result = service.check("essay.txt", "text/plain", 100);

        assertThat(result.status()).isEqualTo(ReportStatus.ACCEPTED);
        assertThat(result.remarks()).isEmpty();
    }

    @Test
    void check_oversizedFile_needsRevision() {
        var result = service.check("big.pdf", "application/pdf", 1_048_577);

        assertThat(result.status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(result.remarks()).anyMatch(r -> r.contains("File size exceeds"));
    }

    @Test
    void check_zipExtension_needsRevision() {
        var result = service.check("archive.zip", "application/zip", 50);

        assertThat(result.status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(result.remarks()).anyMatch(r -> r.contains("ZIP archives"));
    }

    @Test
    void check_missingExtension_needsRevision() {
        var result = service.check("noextension", null, 10);

        assertThat(result.status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(result.remarks()).contains("File extension is missing");
    }

    @Test
    void check_unsupportedExtension_needsRevision() {
        var result = service.check("image.png", "image/png", 10);

        assertThat(result.status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(result.remarks()).anyMatch(r -> r.contains("Unsupported file format"));
    }

    @Test
    void check_zipContentType_needsRevision() {
        var result = service.check("file.txt", "application/x-zip-compressed", 10);

        assertThat(result.status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(result.remarks()).anyMatch(r -> r.contains("ZIP content type"));
    }
}
