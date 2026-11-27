package com.cosmoscan.analysis.api;

import com.cosmoscan.analysis.api.dto.AnalyzeWorkRequest;
import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.api.dto.WordCloudResponse;
import com.cosmoscan.analysis.api.dto.WordCloudTermResponse;
import com.cosmoscan.analysis.domain.ReportStatus;
import com.cosmoscan.analysis.domain.WordCloudStatus;
import com.cosmoscan.analysis.exception.WordCloudNotFoundException;
import com.cosmoscan.analysis.service.WordCloudService;
import com.cosmoscan.analysis.service.WorkAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalysisControllersTest {

    @Mock
    private WorkAnalysisService workAnalysisService;

    @Mock
    private WordCloudService wordCloudService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new InternalAnalysisController(workAnalysisService),
                        new ReportController(workAnalysisService),
                        new WordCloudController(wordCloudService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void analyze_returnsCreatedReport() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(workAnalysisService.analyze(any(AnalyzeWorkRequest.class))).thenReturn(new TechnicalReportResponse(
                reportId, workId, fileId, Instant.parse("2026-05-17T10:00:00Z"), ReportStatus.ACCEPTED, List.of()));

        mockMvc.perform(post("/internal/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workId":"%s","fileId":"%s","studentFullName":"Ivan Ivanov",
                                 "originalFileName":"work.txt","contentType":"text/plain","fileSizeBytes":5}
                                """.formatted(workId, fileId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void analyze_fileMissingInStorage_returnsBadRequest() throws Exception {
        when(workAnalysisService.analyze(any(AnalyzeWorkRequest.class)))
                .thenThrow(new IllegalArgumentException("File not found in File Storing Service"));

        mockMvc.perform(post("/internal/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workId":"%s","fileId":"%s","studentFullName":"Ivan Ivanov",
                                 "originalFileName":"work.txt","fileSizeBytes":5}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("File not found in File Storing Service"));
    }

    @Test
    void getReports_returnsReportsForWork() throws Exception {
        UUID workId = UUID.randomUUID();
        when(workAnalysisService.findReportsByWorkId(workId)).thenReturn(List.of(new TechnicalReportResponse(
                UUID.randomUUID(), workId, UUID.randomUUID(), Instant.parse("2026-05-17T10:00:00Z"),
                ReportStatus.NEEDS_REVISION, List.of("File extension is missing"))));

        mockMvc.perform(get("/works/{workId}/reports", workId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("NEEDS_REVISION"))
                .andExpect(jsonPath("$[0].remarks[0]").value("File extension is missing"));
    }

    @Test
    void getWordCloud_returnsTerms() throws Exception {
        UUID workId = UUID.randomUUID();
        when(wordCloudService.getByWorkId(workId)).thenReturn(new WordCloudResponse(
                workId, UUID.randomUUID(), Instant.parse("2026-05-17T10:00:00Z"), WordCloudStatus.READY,
                List.of(new WordCloudTermResponse("orbit", 3))));

        mockMvc.perform(get("/works/{workId}/word-cloud", workId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.terms[0].text").value("orbit"))
                .andExpect(jsonPath("$.terms[0].weight").value(3));
    }

    @Test
    void getWordCloud_unknownWork_returnsNotFound() throws Exception {
        UUID workId = UUID.randomUUID();
        when(wordCloudService.getByWorkId(workId)).thenThrow(new WordCloudNotFoundException(workId));

        mockMvc.perform(get("/works/{workId}/word-cloud", workId))
                .andExpect(status().isNotFound());
    }
}
