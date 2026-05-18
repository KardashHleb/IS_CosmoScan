package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.WordCloudResponse;
import com.cosmoscan.analysis.domain.WordCloudStatus;
import com.cosmoscan.analysis.domain.WordCloudTerm;
import com.cosmoscan.analysis.domain.WorkWordCloud;
import com.cosmoscan.analysis.exception.WordCloudNotFoundException;
import com.cosmoscan.analysis.repository.WorkWordCloudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordCloudServiceTest {

    @Mock
    private WorkWordCloudRepository wordCloudRepository;

    @Mock
    private FileStoringClient fileStoringClient;

    @Mock
    private TextExtractionService textExtractionService;

    @Mock
    private WordFrequencyService wordFrequencyService;

    @InjectMocks
    private WordCloudService service;

    @Test
    void generateForWork_extractsTextAndPersistsReadyCloud() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        byte[] fileBytes = "content".getBytes(StandardCharsets.UTF_8);
        when(fileStoringClient.download(fileId)).thenReturn(fileBytes);
        when(textExtractionService.extract(fileBytes, "work.txt")).thenReturn("cosmos cosmos orbit");
        when(wordFrequencyService.topTerms("cosmos cosmos orbit"))
                .thenReturn(List.of(
                        new WordFrequencyService.WeightedTerm("cosmos", 2),
                        new WordFrequencyService.WeightedTerm("orbit", 1)
                ));

        service.generateForWork(workId, fileId, "work.txt");

        verify(wordCloudRepository).save(any(WorkWordCloud.class));
    }

    @Test
    void getByWorkId_whenMissing_throwsNotFound() {
        UUID workId = UUID.randomUUID();
        when(wordCloudRepository.findByWorkId(workId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByWorkId(workId))
                .isInstanceOf(WordCloudNotFoundException.class);
    }

    @Test
    void getByWorkId_returnsMappedResponse() {
        UUID workId = UUID.randomUUID();
        WorkWordCloud entity = new WorkWordCloud(
                UUID.randomUUID(),
                workId,
                UUID.randomUUID(),
                Instant.parse("2026-05-17T12:00:00Z"),
                WordCloudStatus.READY,
                List.of(new WordCloudTerm("космос", 5))
        );
        when(wordCloudRepository.findByWorkId(workId)).thenReturn(Optional.of(entity));

        WordCloudResponse response = service.getByWorkId(workId);

        assertThat(response.workId()).isEqualTo(workId);
        assertThat(response.status()).isEqualTo(WordCloudStatus.READY);
        assertThat(response.terms()).hasSize(1);
        assertThat(response.terms().get(0).text()).isEqualTo("космос");
        assertThat(response.terms().get(0).weight()).isEqualTo(5);
    }
}
