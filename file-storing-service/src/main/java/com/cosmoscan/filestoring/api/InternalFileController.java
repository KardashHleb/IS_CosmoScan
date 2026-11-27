package com.cosmoscan.filestoring.api;

import com.cosmoscan.filestoring.service.FileStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/internal/files")
@Tag(name = "Internal files", description = "Called by submission-service only; not exposed through API Gateway")
public class InternalFileController {

    private final FileStoreService fileStoreService;

    public InternalFileController(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }

    @Operation(summary = "Delete a stored file",
            description = "Compensating action: removes a file whose work submission failed after the upload.")
    @ApiResponse(responseCode = "204", description = "File deleted")
    @ApiResponse(responseCode = "404", description = "File not found")
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "File id returned from upload", required = true) @PathVariable UUID fileId
    ) throws IOException {
        fileStoreService.delete(fileId);
    }
}
