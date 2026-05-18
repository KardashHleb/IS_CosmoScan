package com.cosmoscan.analysis.api;

import com.cosmoscan.analysis.api.dto.AnalyzeWorkRequest;
import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.service.WorkAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/internal/analysis")
@Tag(name = "Internal analysis", description = "Called by submission-service only; not exposed through API Gateway")
public class InternalAnalysisController {

    private final WorkAnalysisService workAnalysisService;

    public InternalAnalysisController(WorkAnalysisService workAnalysisService) {
        this.workAnalysisService = workAnalysisService;
    }

    @Operation(summary = "Run technical analysis",
            description = "Verifies the file exists in file-storing, runs technical checks, persists report to DB and disk.")
    @ApiResponse(responseCode = "201", description = "Report created",
            content = @Content(schema = @Schema(implementation = TechnicalReportResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or file not found")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicalReportResponse analyze(@Valid @RequestBody AnalyzeWorkRequest request) throws IOException {
        return workAnalysisService.analyze(request);
    }
}
