package com.cosmoscan.submission.service;

import com.cosmoscan.submission.api.dto.WorkResponse;
import com.cosmoscan.submission.client.AnalyzeWorkRequest;
import com.cosmoscan.submission.client.AnalysisClient;
import com.cosmoscan.submission.client.FileStoringClient;
import com.cosmoscan.submission.client.StoredFileResponse;
import com.cosmoscan.submission.client.TechnicalReportResponse;
import com.cosmoscan.submission.domain.Work;
import com.cosmoscan.submission.repository.WorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class WorkSubmissionService {

    private final WorkRepository workRepository;
    private final FileStoringClient fileStoringClient;
    private final AnalysisClient analysisClient;

    public WorkSubmissionService(
            WorkRepository workRepository,
            FileStoringClient fileStoringClient,
            AnalysisClient analysisClient
    ) {
        this.workRepository = workRepository;
        this.fileStoringClient = fileStoringClient;
        this.analysisClient = analysisClient;
    }

    @Transactional
    public WorkResponse submit(String studentFullName, MultipartFile file) throws IOException {
        UUID workId = UUID.randomUUID();
        StoredFileResponse storedFile = fileStoringClient.store(file);

        Work work = new Work(
                workId,
                studentFullName.trim(),
                Instant.now(),
                storedFile.originalFileName(),
                storedFile.fileId(),
                storedFile.contentType(),
                storedFile.fileSizeBytes()
        );
        Work saved = workRepository.save(work);

        TechnicalReportResponse report = analysisClient.analyze(new AnalyzeWorkRequest(
                saved.getId(),
                saved.getFileId(),
                saved.getStudentFullName(),
                saved.getOriginalFileName(),
                saved.getContentType(),
                saved.getFileSizeBytes()
        ));

        return WorkResponse.from(saved, report);
    }
}
