package com.cosmoscan.submission.api;

import com.cosmoscan.submission.api.dto.WorkResponse;
import com.cosmoscan.submission.service.WorkSubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkControllerTest {

    @Mock
    private WorkSubmissionService workSubmissionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkController(workSubmissionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitWork_validFile_returnsCreatedWithReport() throws Exception {
        UUID workId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "essay.txt", "text/plain", "hello".getBytes());
        WorkResponse response = new WorkResponse(
                workId, "Ivan Ivanov", Instant.parse("2026-05-17T10:00:00Z"), "essay.txt", fileId, 5, "text/plain",
                new WorkResponse.TechnicalReportSummary(
                        UUID.randomUUID(), "ACCEPTED", List.of(), Instant.parse("2026-05-17T10:00:01Z")));
        when(workSubmissionService.submit(eq("Ivan Ivanov"), any())).thenReturn(response);

        mockMvc.perform(multipart("/works").file(file).param("studentFullName", "Ivan Ivanov"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workId.toString()))
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.technicalReport.status").value("ACCEPTED"));
    }

    @Test
    void submitWork_disallowedFile_returnsBadRequestWithoutCallingService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/works").file(file).param("studentFullName", "Ivan Ivanov"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported file format. Allowed: pdf, docx, txt"));

        verifyNoInteractions(workSubmissionService);
    }
}
