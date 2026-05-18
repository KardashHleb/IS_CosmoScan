package com.cosmoscan.filestoring.validation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllowedFileValidatorTest {

    @Test
    void validate_validPdf_passes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        assertThatCode(() -> AllowedFileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void validate_missingExtension_throws() {
        assertThatThrownBy(() -> AllowedFileValidator.validate("readme", "text/plain", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extensions");
    }
}
