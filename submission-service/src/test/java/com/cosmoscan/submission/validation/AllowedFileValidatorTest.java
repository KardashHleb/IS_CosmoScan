package com.cosmoscan.submission.validation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllowedFileValidatorTest {

    @Test
    void validate_validTxtFile_passes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "work.txt", "text/plain", "content".getBytes()
        );

        assertThatCode(() -> AllowedFileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "work.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> AllowedFileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    void validate_oversizedFile_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("big.txt", "text/plain", 1_048_577))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    void validate_zipContentType_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("file.txt", "application/zip", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZIP files are not allowed");
    }

    @Test
    void validate_unsupportedExtension_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("image.png", "image/png", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file format");
    }
}
