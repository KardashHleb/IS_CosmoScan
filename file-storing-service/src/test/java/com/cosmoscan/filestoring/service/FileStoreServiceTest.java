package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.config.StorageProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStoreServiceTest {

    @TempDir
    Path tempDir;

    private FileStoreService fileStoreService;

    @BeforeEach
    void setUp() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setFilesPath(tempDir.toString());
        fileStoreService = new FileStoreService(properties, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void storeAndLoad_roundTrip() throws Exception {
        MockMultipartFile upload = new MockMultipartFile(
                "file", "sample.txt", "text/plain", "cosmoscan".getBytes()
        );

        var stored = fileStoreService.store(upload);
        FileStoreService.StoredFileResource loaded = fileStoreService.load(stored.fileId());

        assertThat(stored.originalFileName()).isEqualTo("sample.txt");
        assertThat(loaded.originalFileName()).isEqualTo("sample.txt");
        assertThat(loaded.mediaType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(loaded.resource().contentLength()).isEqualTo(9);
    }

    @Test
    void load_unknownId_throwsFileNotFound() {
        assertThatThrownBy(() -> fileStoreService.load(UUID.randomUUID()))
                .isInstanceOf(FileStoreService.FileNotFoundException.class);
    }

    @Test
    void store_invalidMultipart_throws() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileStoreService.store(empty))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
