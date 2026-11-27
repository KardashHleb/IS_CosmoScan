package com.cosmoscan.filevalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FileRulesTest {

    @ParameterizedTest
    @CsvSource({
            "work.pdf, pdf",
            "WORK.DOCX, docx",
            "archive.tar.gz, gz",
            "notes.Txt, txt"
    })
    void extractExtension_returnsLowerCaseExtension(String fileName, String expected) {
        assertThat(FileRules.extractExtension(fileName)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"readme", "trailing-dot."})
    void extractExtension_withoutExtension_returnsEmpty(String fileName) {
        assertThat(FileRules.extractExtension(fileName)).isEmpty();
    }

    @Test
    void isAllowedExtension_acceptsOnlyPdfDocxTxt() {
        assertThat(FileRules.isAllowedExtension("pdf")).isTrue();
        assertThat(FileRules.isAllowedExtension("docx")).isTrue();
        assertThat(FileRules.isAllowedExtension("txt")).isTrue();
        assertThat(FileRules.isAllowedExtension("png")).isFalse();
        assertThat(FileRules.isAllowedExtension("zip")).isFalse();
    }

    @Test
    void exceedsMaxSize_isTrueOnlyAboveOneMebibyte() {
        assertThat(FileRules.exceedsMaxSize(FileRules.MAX_FILE_SIZE_BYTES)).isFalse();
        assertThat(FileRules.exceedsMaxSize(FileRules.MAX_FILE_SIZE_BYTES + 1)).isTrue();
    }

    @Test
    void isZipContentType_detectsZipCaseInsensitively() {
        assertThat(FileRules.isZipContentType("application/zip")).isTrue();
        assertThat(FileRules.isZipContentType("application/X-ZIP-Compressed")).isTrue();
        assertThat(FileRules.isZipContentType("text/plain")).isFalse();
        assertThat(FileRules.isZipContentType(null)).isFalse();
    }
}
