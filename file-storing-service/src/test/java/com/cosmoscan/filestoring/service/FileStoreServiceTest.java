package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.config.StorageProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
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
    void load_withoutContentType_fallsBackToOctetStream() throws Exception {
        var stored = fileStoreService.store(new MockMultipartFile("file", "sample.txt", null, "x".getBytes()));

        assertThat(fileStoreService.load(stored.fileId()).mediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void load_contentMissingButMetadataPresent_throwsFileNotFound() throws Exception {
        var stored = fileStoreService.store(new MockMultipartFile("file", "sample.txt", "text/plain", "x".getBytes()));
        Files.delete(tempDir.resolve(stored.fileId().toString()).resolve("sample.txt"));

        assertThatThrownBy(() -> fileStoreService.load(stored.fileId()))
                .isInstanceOf(FileStoreService.FileNotFoundException.class)
                .hasMessageContaining("File content not found");
    }

    @Test
    void store_fileNameWithParentDirectory_throws() {
        MockMultipartFile upload = new MockMultipartFile("file", "../escape.txt", "text/plain", "x".getBytes());

        assertThatThrownBy(() -> fileStoreService.store(upload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file name");
    }

    @Test
    void store_absoluteFileName_throws() {
        MockMultipartFile upload = new MockMultipartFile("file", "/absolute/escape.txt", "text/plain", "x".getBytes());

        assertThatThrownBy(() -> fileStoreService.store(upload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid file path");
    }

    @Test
    void delete_removesFileAndMetadata() throws Exception {
        var stored = fileStoreService.store(new MockMultipartFile("file", "sample.txt", "text/plain", "x".getBytes()));

        fileStoreService.delete(stored.fileId());

        assertThat(tempDir.resolve(stored.fileId().toString())).doesNotExist();
        assertThatThrownBy(() -> fileStoreService.load(stored.fileId()))
                .isInstanceOf(FileStoreService.FileNotFoundException.class);
    }

    @Test
    void delete_unknownId_throwsFileNotFound() {
        assertThatThrownBy(() -> fileStoreService.delete(UUID.randomUUID()))
                .isInstanceOf(FileStoreService.FileNotFoundException.class);
    }

    @Test
    void store_invalidMultipart_throws() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileStoreService.store(empty))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
