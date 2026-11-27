package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.WordCloudResponse;
import com.cosmoscan.analysis.domain.WordCloudStatus;
import com.cosmoscan.analysis.domain.WordCloudTerm;
import com.cosmoscan.analysis.domain.WorkWordCloud;
import com.cosmoscan.analysis.exception.WordCloudNotFoundException;
import com.cosmoscan.analysis.repository.WorkWordCloudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verifyNoInteractions;
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  \n ")
    void generateForWork_noExtractedText_persistsNoTextCloud(String extractedText) throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        byte[] fileBytes = "content".getBytes(StandardCharsets.UTF_8);
        when(fileStoringClient.download(fileId)).thenReturn(fileBytes);
        when(textExtractionService.extract(fileBytes, "work.pdf")).thenReturn(extractedText);
        when(wordCloudRepository.findByWorkId(workId)).thenReturn(Optional.empty());

        service.generateForWork(workId, fileId, "work.pdf");

        WorkWordCloud saved = captureSavedCloud();
        assertThat(saved.getStatus()).isEqualTo(WordCloudStatus.NO_TEXT);
        assertThat(saved.getTerms()).isEmpty();
        verifyNoInteractions(wordFrequencyService);
    }

    @Test
    void generateForWork_onlyStopWords_persistsNoTextCloud() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        byte[] fileBytes = "the and".getBytes(StandardCharsets.UTF_8);
        when(fileStoringClient.download(fileId)).thenReturn(fileBytes);
        when(textExtractionService.extract(fileBytes, "work.txt")).thenReturn("the and");
        when(wordFrequencyService.topTerms("the and")).thenReturn(List.of());

        service.generateForWork(workId, fileId, "work.txt");

        assertThat(captureSavedCloud().getStatus()).isEqualTo(WordCloudStatus.NO_TEXT);
    }

    @Test
    void generateForWork_downloadFails_persistsFailedCloud() {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        when(fileStoringClient.download(fileId)).thenThrow(new IllegalStateException("file-storing is down"));

        service.generateForWork(workId, fileId, "work.txt");

        WorkWordCloud saved = captureSavedCloud();
        assertThat(saved.getStatus()).isEqualTo(WordCloudStatus.FAILED);
        assertThat(saved.getTerms()).isEmpty();
    }

    @Test
    void generateForWork_existingCloud_isReplaced() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        WorkWordCloud previous = new WorkWordCloud(UUID.randomUUID(), workId, fileId,
                Instant.parse("2026-05-17T12:00:00Z"), WordCloudStatus.FAILED, List.of());
        byte[] fileBytes = "orbit".getBytes(StandardCharsets.UTF_8);
        when(wordCloudRepository.findByWorkId(workId)).thenReturn(Optional.of(previous));
        when(fileStoringClient.download(fileId)).thenReturn(fileBytes);
        when(textExtractionService.extract(fileBytes, "work.txt")).thenReturn("orbit");
        when(wordFrequencyService.topTerms("orbit")).thenReturn(List.of(new WordFrequencyService.WeightedTerm("orbit", 1)));

        service.generateForWork(workId, fileId, "work.txt");

        verify(wordCloudRepository).delete(previous);
        WorkWordCloud saved = captureSavedCloud();
        assertThat(saved.getStatus()).isEqualTo(WordCloudStatus.READY);
        assertThat(saved.getTerms()).extracting(WordCloudTerm::getText).containsExactly("orbit");
    }

    private WorkWordCloud captureSavedCloud() {
        ArgumentCaptor<WorkWordCloud> captor = ArgumentCaptor.forClass(WorkWordCloud.class);
        verify(wordCloudRepository).save(captor.capture());
        return captor.getValue();
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
