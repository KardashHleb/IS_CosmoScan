package com.cosmoscan.filestoring.api;

import com.cosmoscan.filestoring.api.dto.StoredFileResponse;
import com.cosmoscan.filestoring.service.FileStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "Upload and download stored work files")
public class FileController {

    private final FileStoreService fileStoreService;

    public FileController(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }

    @Operation(summary = "Upload a file", description = "Stores file on disk and returns metadata with a new fileId.")
    @ApiResponse(responseCode = "201", description = "File stored",
            content = @Content(schema = @Schema(implementation = StoredFileResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or disallowed file")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StoredFileResponse upload(
            @Parameter(description = "File to store: pdf, docx, or txt; max 1 MiB", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return fileStoreService.store(file);
    }

    @Operation(summary = "Download a file by id")
    @ApiResponse(responseCode = "200", description = "File content (attachment)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> download(
            @Parameter(description = "File id returned from upload", required = true) @PathVariable UUID fileId
    ) throws IOException {
        FileStoreService.StoredFileResource stored = fileStoreService.load(fileId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(stored.originalFileName())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(stored.mediaType())
                .body(stored.resource());
    }
}
