package com.cosmoscan.filevalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllowedFileValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"work.pdf", "work.docx", "work.txt"})
    void validate_allowedFile_passes(String fileName) {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", new byte[]{1});

        assertThatCode(() -> AllowedFileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "work.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> AllowedFileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File must not be empty");
    }

    @Test
    void validate_fileWithoutOriginalName_throwsMissingExtension() {
        MockMultipartFile file = new MockMultipartFile("file", null, "text/plain", new byte[]{1});

        assertThatThrownBy(() -> AllowedFileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only files with extensions");
    }

    @Test
    void validate_sizeAtLimit_passes() {
        assertThatCode(() -> AllowedFileValidator.validate("work.txt", "text/plain", FileRules.MAX_FILE_SIZE_BYTES))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_oversizedFile_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("big.txt", "text/plain", FileRules.MAX_FILE_SIZE_BYTES + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds 1 MiB limit");
    }

    @Test
    void validate_missingExtension_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("readme", "text/plain", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only files with extensions");
    }

    @Test
    void validate_unsupportedExtension_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("image.png", "image/png", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file format");
    }

    @Test
    void validate_zipContentType_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("file.txt", "application/zip", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ZIP files are not allowed");
    }

    @Test
    void validate_nullContentType_passes() {
        assertThatCode(() -> AllowedFileValidator.validate("file.txt", null, 10)).doesNotThrowAnyException();
    }
}
