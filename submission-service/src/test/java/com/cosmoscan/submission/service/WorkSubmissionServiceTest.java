package com.cosmoscan.submission.service;

import com.cosmoscan.submission.client.AnalyzeWorkRequest;
import com.cosmoscan.submission.client.AnalysisClient;
import com.cosmoscan.submission.client.FileStoringClient;
import com.cosmoscan.submission.client.StoredFileResponse;
import com.cosmoscan.submission.client.TechnicalReportResponse;
import com.cosmoscan.submission.domain.Work;
import com.cosmoscan.submission.repository.WorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkSubmissionServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private FileStoringClient fileStoringClient;

    @Mock
    private AnalysisClient analysisClient;

    @InjectMocks
    private WorkSubmissionService service;

    @Test
    void submit_storesFileSavesWorkAndReturnsResponse() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "essay.txt", "text/plain", "hello".getBytes()
        );
        when(fileStoringClient.store(multipart)).thenReturn(new StoredFileResponse(
                fileId, "essay.txt", "text/plain", 5, Instant.parse("2026-05-17T10:00:00Z")
        ));
        when(workRepository.save(any(Work.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisClient.analyze(any(AnalyzeWorkRequest.class))).thenAnswer(invocation -> {
            AnalyzeWorkRequest req = invocation.getArgument(0);
            return new TechnicalReportResponse(
                    reportId,
                    req.workId(),
                    fileId,
                    Instant.parse("2026-05-17T10:00:01Z"),
                    "ACCEPTED",
                    List.of()
            );
        });

        var response = service.submit("  Ivan Ivanov  ", multipart);

        assertThat(response.studentFullName()).isEqualTo("Ivan Ivanov");
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.technicalReport().status()).isEqualTo("ACCEPTED");
        assertThat(response.technicalReport().id()).isEqualTo(reportId);

        ArgumentCaptor<Work> workCaptor = ArgumentCaptor.forClass(Work.class);
        verify(workRepository).save(workCaptor.capture());
        assertThat(workCaptor.getValue().getStudentFullName()).isEqualTo("Ivan Ivanov");
        assertThat(workCaptor.getValue().getFileId()).isEqualTo(fileId);
    }
}
