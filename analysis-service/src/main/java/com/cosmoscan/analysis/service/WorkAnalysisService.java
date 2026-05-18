package com.cosmoscan.analysis.service;

import com.cosmoscan.analysis.api.dto.AnalyzeWorkRequest;
import com.cosmoscan.analysis.api.dto.TechnicalReportResponse;
import com.cosmoscan.analysis.domain.TechnicalReport;
import com.cosmoscan.analysis.repository.TechnicalReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WorkAnalysisService {

    private final TechnicalReportRepository reportRepository;
    private final TechnicalCheckService technicalCheckService;
    private final ReportFileWriter reportFileWriter;
    private final FileStoringClient fileStoringClient;
    private final WordCloudService wordCloudService;

    public WorkAnalysisService(
            TechnicalReportRepository reportRepository,
            TechnicalCheckService technicalCheckService,
            ReportFileWriter reportFileWriter,
            FileStoringClient fileStoringClient,
            WordCloudService wordCloudService
    ) {
        this.reportRepository = reportRepository;
        this.technicalCheckService = technicalCheckService;
        this.reportFileWriter = reportFileWriter;
        this.fileStoringClient = fileStoringClient;
        this.wordCloudService = wordCloudService;
    }

    @Transactional
    public TechnicalReportResponse analyze(AnalyzeWorkRequest request) throws IOException {
        if (!fileStoringClient.fileExists(request.fileId())) {
            throw new IllegalArgumentException("File not found in File Storing Service: " + request.fileId());
        }

        TechnicalCheckService.TechnicalCheckResult checkResult = technicalCheckService.check(
                request.originalFileName(),
                request.contentType(),
                request.fileSizeBytes()
        );

        UUID reportId = UUID.randomUUID();
        TechnicalReportResponse response = new TechnicalReportResponse(
                reportId,
                request.workId(),
                request.fileId(),
                Instant.now(),
                checkResult.status(),
                checkResult.remarks()
        );

        String reportFilePath = reportFileWriter.write(reportId, response);

        TechnicalReport entity = new TechnicalReport(
                reportId,
                request.workId(),
                request.fileId(),
                response.checkedAt(),
                checkResult.status(),
                checkResult.remarks(),
                reportFilePath
        );
        reportRepository.save(entity);

        wordCloudService.generateForWork(request.workId(), request.fileId(), request.originalFileName());

        return response;
    }

    @Transactional(readOnly = true)
    public List<TechnicalReportResponse> findReportsByWorkId(UUID workId) {
        return reportRepository.findByWorkIdOrderByCheckedAtDesc(workId).stream()
                .map(TechnicalReportResponse::from)
                .toList();
    }
}
