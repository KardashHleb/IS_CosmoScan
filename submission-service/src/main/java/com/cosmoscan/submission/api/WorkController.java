package com.cosmoscan.submission.api;

import com.cosmoscan.submission.api.dto.WorkResponse;
import com.cosmoscan.submission.service.WorkSubmissionService;
import com.cosmoscan.submission.validation.AllowedFileValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/works")
@Validated
@Tag(name = "Works", description = "Submit student works and receive a technical report summary")
public class WorkController {

    private final WorkSubmissionService workSubmissionService;

    public WorkController(WorkSubmissionService workSubmissionService) {
        this.workSubmissionService = workSubmissionService;
    }

    @Operation(
            summary = "Submit a work",
            description = "Validates the file, stores it in file-storing, saves work metadata, and runs technical analysis."
    )
    @ApiResponse(responseCode = "201", description = "Work accepted and analyzed",
            content = @Content(schema = @Schema(implementation = WorkResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid file or request")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkResponse submitWork(
            @Parameter(description = "Student full name", required = true, example = "Ivan Ivanov")
            @RequestParam("studentFullName") @NotBlank String studentFullName,
            @Parameter(description = "Work file: pdf, docx, or txt; max 1 MiB", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        AllowedFileValidator.validate(file);
        return workSubmissionService.submit(studentFullName, file);
    }
}
