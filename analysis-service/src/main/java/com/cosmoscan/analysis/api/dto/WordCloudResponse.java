package com.cosmoscan.analysis.api.dto;

import com.cosmoscan.analysis.domain.WordCloudStatus;
import com.cosmoscan.analysis.domain.WorkWordCloud;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WordCloudResponse(
        UUID workId,
        UUID fileId,
        Instant generatedAt,
        WordCloudStatus status,
        List<WordCloudTermResponse> terms
) {

    public static WordCloudResponse from(WorkWordCloud wordCloud) {
        List<WordCloudTermResponse> terms = wordCloud.getTerms().stream()
                .map(term -> new WordCloudTermResponse(term.getText(), term.getWeight()))
                .toList();
        return new WordCloudResponse(
                wordCloud.getWorkId(),
                wordCloud.getFileId(),
                wordCloud.getGeneratedAt(),
                wordCloud.getStatus(),
                terms
        );
    }
}
