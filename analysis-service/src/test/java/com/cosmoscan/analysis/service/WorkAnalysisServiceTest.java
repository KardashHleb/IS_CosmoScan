package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.AnalyzeWorkRequest;
import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.domain.ReportStatus;
import com.cosmoscan.analysis.domain.TechnicalReport;
import com.cosmoscan.analysis.repository.TechnicalReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkAnalysisServiceTest {

    @Mock
    private TechnicalReportRepository reportRepository;

    @Mock
    private TechnicalCheckService technicalCheckService;

    @Mock
    private ReportFileWriter reportFileWriter;

    @Mock
    private FileStoringClient fileStoringClient;

    @Mock
    private WordCloudService wordCloudService;

    @InjectMocks
    private WorkAnalysisService service;

    @Test
    void analyze_whenFileNotFound_throwsIllegalArgumentException() {
        UUID fileId = UUID.randomUUID();
        when(fileStoringClient.fileExists(fileId)).thenReturn(false);

        var request = new AnalyzeWorkRequest(
                UUID.randomUUID(), fileId, "Student", "work.txt", "text/plain", 10
        );

        assertThatThrownBy(() -> service.analyze(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(fileId.toString());
    }

    @Test
    void analyze_validRequest_persistsReportAndReturnsResponse() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        when(fileStoringClient.fileExists(fileId)).thenReturn(true);
        when(technicalCheckService.check("work.txt", "text/plain", 20))
                .thenReturn(new TechnicalCheckService.TechnicalCheckResult(ReportStatus.ACCEPTED, List.of()));
        when(reportFileWriter.write(any(), any())).thenReturn("/reports/test.json");

        TechnicalReportResponse response = service.analyze(new AnalyzeWorkRequest(
                workId, fileId, "Student", "work.txt", "text/plain", 20
        ));

        assertThat(response.workId()).isEqualTo(workId);
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.status()).isEqualTo(ReportStatus.ACCEPTED);

        ArgumentCaptor<TechnicalReport> captor = ArgumentCaptor.forClass(TechnicalReport.class);
        verify(reportRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkId()).isEqualTo(workId);
        assertThat(captor.getValue().getReportFilePath()).isEqualTo("/reports/test.json");

        verify(wordCloudService).generateForWork(workId, fileId, "work.txt");
    }

    @Test
    void findReportsByWorkId_mapsEntitiesToResponses() {
        UUID workId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Instant checkedAt = Instant.parse("2026-05-17T10:00:00Z");
        TechnicalReport entity = new TechnicalReport(
                reportId, workId, UUID.randomUUID(), checkedAt,
                ReportStatus.NEEDS_REVISION, List.of("remark"), "/path.json"
        );
        when(reportRepository.findByWorkIdOrderByCheckedAtDesc(workId)).thenReturn(List.of(entity));

        List<TechnicalReportResponse> reports = service.findReportsByWorkId(workId);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).id()).isEqualTo(reportId);
        assertThat(reports.get(0).status()).isEqualTo(ReportStatus.NEEDS_REVISION);
        assertThat(reports.get(0).remarks()).containsExactly("remark");
    }
}
