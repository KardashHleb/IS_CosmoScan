package com.cosmoscan.analysis.api;

import com.cosmoscan.analysis.api.dto.WordCloudResponse;
import com.cosmoscan.analysis.service.WordCloudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/works")
@Tag(name = "Word cloud (public)", description = "Word-frequency data for visualizing submitted work (via API Gateway)")
public class WordCloudController {

    private final WordCloudService wordCloudService;

    public WordCloudController(WordCloudService wordCloudService) {
        this.wordCloudService = wordCloudService;
    }

    @Operation(
            summary = "Get word cloud data for a work",
            description = "Returns ranked terms and weights for client-side word-cloud rendering. "
                    + "Generated during technical analysis after work submission."
    )
    @ApiResponse(responseCode = "200", description = "Word cloud found",
            content = @Content(schema = @Schema(implementation = WordCloudResponse.class)))
    @ApiResponse(responseCode = "404", description = "Word cloud not found for this work")
    @GetMapping("/{workId}/word-cloud")
    public WordCloudResponse getWordCloud(
            @Parameter(description = "Work id from submission service", required = true) @PathVariable UUID workId
    ) {
        return wordCloudService.getByWorkId(workId);
    }
}
