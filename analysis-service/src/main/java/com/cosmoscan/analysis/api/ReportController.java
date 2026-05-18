package com.cosmoscan.analysis.api;

import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.service.WorkAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/works")
@Tag(name = "Technical reports (public)", description = "Read technical reports for a work (exposed via API Gateway)")
public class ReportController {

    private final WorkAnalysisService workAnalysisService;

    public ReportController(WorkAnalysisService workAnalysisService) {
        this.workAnalysisService = workAnalysisService;
    }

    @Operation(summary = "List technical reports for a work",
            description = "Returns all technical reports for the given work id, newest first.")
    @ApiResponse(responseCode = "200", description = "Reports found (may be empty)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TechnicalReportResponse.class))))
    @GetMapping("/{workId}/reports")
    public List<TechnicalReportResponse> getReports(
            @Parameter(description = "Work id from submission service", required = true) @PathVariable UUID workId
    ) {
        return workAnalysisService.findReportsByWorkId(workId);
    }
}
