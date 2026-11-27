package com.cosmoscan.submission.service;

import com.cosmoscan.submission.api.dto.WorkResponse;
import com.cosmoscan.submission.client.AnalyzeWorkRequest;
import com.cosmoscan.submission.client.AnalysisClient;
import com.cosmoscan.submission.client.FileStoringClient;
import com.cosmoscan.submission.client.StoredFileResponse;
import com.cosmoscan.submission.client.TechnicalReportResponse;
import com.cosmoscan.submission.domain.Work;
import com.cosmoscan.submission.repository.WorkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class WorkSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(WorkSubmissionService.class);

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

    /**
     * Stores the file, saves the work and runs the analysis. If anything fails after the file is
     * stored, the work row is rolled back by the transaction and the stored file is deleted
     * (compensating action), so no orphan file is left in file-storing-service.
     */
    @Transactional
    public WorkResponse submit(String studentFullName, MultipartFile file) throws IOException {
        StoredFileResponse storedFile = fileStoringClient.store(file);
        try {
            return saveAndAnalyze(studentFullName, storedFile);
        } catch (RuntimeException ex) {
            deleteStoredFile(storedFile, ex);
            throw ex;
        }
    }

    private void deleteStoredFile(StoredFileResponse storedFile, RuntimeException cause) {
        try {
            fileStoringClient.delete(storedFile.fileId());
            log.info("Deleted file {} after failed submission", storedFile.fileId());
        } catch (RuntimeException cleanupEx) {
            log.warn("Could not delete file {} after failed submission", storedFile.fileId(), cleanupEx);
            cause.addSuppressed(cleanupEx);
        }
    }

    private WorkResponse saveAndAnalyze(String studentFullName, StoredFileResponse storedFile) {
        UUID workId = UUID.randomUUID();
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
