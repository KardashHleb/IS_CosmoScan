package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.api.dto.StoredFileResponse;
import com.cosmoscan.filestoring.config.StorageProperties;
import com.cosmoscan.filestoring.domain.StoredFileMetadata;
import com.cosmoscan.filestoring.validation.AllowedFileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStoreService {

    private static final String METADATA_FILE = "metadata.json";

    private final Path filesRoot;
    private final ObjectMapper objectMapper;

    public FileStoreService(StorageProperties storageProperties, ObjectMapper objectMapper) throws IOException {
        this.filesRoot = Path.of(storageProperties.getFilesPath()).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        Files.createDirectories(this.filesRoot);
    }

    public StoredFileResponse store(MultipartFile file) throws IOException {
        AllowedFileValidator.validate(file);

        UUID fileId = UUID.randomUUID();
        String originalFileName = sanitizeFileName(file.getOriginalFilename());

        Path fileDirectory = filesRoot.resolve(fileId.toString());
        Path targetFile = fileDirectory.resolve(originalFileName).normalize();
        if (!targetFile.startsWith(fileDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        Files.createDirectories(fileDirectory);
        file.transferTo(targetFile);

        StoredFileMetadata metadata = new StoredFileMetadata(
                fileId,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                targetFile.toString(),
                Instant.now()
        );
        objectMapper.writeValue(fileDirectory.resolve(METADATA_FILE).toFile(), metadata);

        return StoredFileResponse.from(metadata);
    }

    public StoredFileResource load(UUID fileId) throws IOException {
        StoredFileMetadata metadata = readMetadata(fileId);
        Path filePath = Path.of(metadata.storagePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File content not found for id: " + fileId);
        }

        Resource resource = toResource(filePath);
        MediaType mediaType = metadata.contentType() != null
                ? MediaType.parseMediaType(metadata.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return new StoredFileResource(resource, metadata.originalFileName(), mediaType);
    }

    private StoredFileMetadata readMetadata(UUID fileId) throws IOException {
        Path metadataPath = filesRoot.resolve(fileId.toString()).resolve(METADATA_FILE);
        if (!Files.exists(metadataPath)) {
            throw new FileNotFoundException("File not found: " + fileId);
        }
        return objectMapper.readValue(metadataPath.toFile(), StoredFileMetadata.class);
    }

    private static Resource toResource(Path filePath) throws MalformedURLException {
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Unreadable file: " + filePath);
        }
        return resource;
    }

    private static String sanitizeFileName(String fileName) {
        String sanitized = StringUtils.cleanPath(fileName);
        if (sanitized.contains("..")) {
            throw new IllegalArgumentException("Invalid file name: " + sanitized);
        }
        return sanitized;
    }

    public record StoredFileResource(Resource resource, String originalFileName, MediaType mediaType) {
    }

    public static class FileNotFoundException extends RuntimeException {
        public FileNotFoundException(String message) {
            super(message);
        }
    }
}
