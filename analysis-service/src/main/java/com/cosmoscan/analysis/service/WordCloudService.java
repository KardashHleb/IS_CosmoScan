package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.WordCloudResponse;
import com.cosmoscan.analysis.domain.WordCloudStatus;
import com.cosmoscan.analysis.domain.WordCloudTerm;
import com.cosmoscan.analysis.domain.WorkWordCloud;
import com.cosmoscan.analysis.exception.WordCloudNotFoundException;
import com.cosmoscan.analysis.repository.WorkWordCloudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WordCloudService {

    private static final Logger log = LoggerFactory.getLogger(WordCloudService.class);

    private final WorkWordCloudRepository wordCloudRepository;
    private final FileStoringClient fileStoringClient;
    private final TextExtractionService textExtractionService;
    private final WordFrequencyService wordFrequencyService;

    public WordCloudService(
            WorkWordCloudRepository wordCloudRepository,
            FileStoringClient fileStoringClient,
            TextExtractionService textExtractionService,
            WordFrequencyService wordFrequencyService
    ) {
        this.wordCloudRepository = wordCloudRepository;
        this.fileStoringClient = fileStoringClient;
        this.textExtractionService = textExtractionService;
        this.wordFrequencyService = wordFrequencyService;
    }

    @Transactional
    public void generateForWork(UUID workId, UUID fileId, String originalFileName) {
        UUID wordCloudId = UUID.randomUUID();
        Instant generatedAt = Instant.now();
        WordCloudStatus status;
        List<WordCloudTerm> terms;

        try {
            byte[] content = fileStoringClient.download(fileId);
            String extractedText = textExtractionService.extract(content, originalFileName);
            if (extractedText == null || extractedText.isBlank()) {
                status = WordCloudStatus.NO_TEXT;
                terms = List.of();
            } else {
                List<WordFrequencyService.WeightedTerm> weightedTerms =
                        wordFrequencyService.topTerms(extractedText);
                if (weightedTerms.isEmpty()) {
                    status = WordCloudStatus.NO_TEXT;
                    terms = List.of();
                } else {
                    status = WordCloudStatus.READY;
                    terms = weightedTerms.stream()
                            .map(term -> new WordCloudTerm(term.text(), term.weight()))
                            .toList();
                }
            }
        } catch (Exception ex) {
            log.warn("Word cloud generation failed for workId={}, fileId={}", workId, fileId, ex);
            status = WordCloudStatus.FAILED;
            terms = List.of();
        }

        wordCloudRepository.findByWorkId(workId).ifPresent(wordCloudRepository::delete);

        WorkWordCloud entity = new WorkWordCloud(
                wordCloudId,
                workId,
                fileId,
                generatedAt,
                status,
                terms
        );
        wordCloudRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public WordCloudResponse getByWorkId(UUID workId) {
        return wordCloudRepository.findByWorkId(workId)
                .map(WordCloudResponse::from)
                .orElseThrow(() -> new WordCloudNotFoundException(workId));
    }
}
