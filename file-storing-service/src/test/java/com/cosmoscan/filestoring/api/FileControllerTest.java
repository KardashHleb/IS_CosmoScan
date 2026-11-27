package com.cosmoscan.filestoring.api;

import com.cosmoscan.filestoring.api.dto.StoredFileResponse;
import com.cosmoscan.filestoring.service.FileStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileStoreService fileStoreService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FileController(fileStoreService), new InternalFileController(fileStoreService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void upload_returnsCreatedWithMetadata() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(fileStoreService.store(any())).thenReturn(new StoredFileResponse(
                fileId, "work.txt", "text/plain", 5, Instant.parse("2026-05-17T10:00:00Z")));

        mockMvc.perform(multipart("/files")
                        .file(new MockMultipartFile("file", "work.txt", "text/plain", "hello".getBytes())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.originalFileName").value("work.txt"));
    }

    @Test
    void upload_invalidFile_returnsBadRequest() throws Exception {
        when(fileStoreService.store(any())).thenThrow(new IllegalArgumentException("ZIP files are not allowed"));

        mockMvc.perform(multipart("/files")
                        .file(new MockMultipartFile("file", "work.txt", "application/zip", new byte[]{1})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ZIP files are not allowed"));
    }

    @Test
    void download_returnsContentAsAttachment() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(fileStoreService.load(fileId)).thenReturn(new FileStoreService.StoredFileResource(
                new ByteArrayResource("hello".getBytes()), "work.txt", MediaType.TEXT_PLAIN));

        mockMvc.perform(get("/files/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("work.txt")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("hello"));
    }

    @Test
    void download_unknownFile_returnsNotFound() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(fileStoreService.load(fileId)).thenThrow(new FileStoreService.FileNotFoundException("File not found: " + fileId));

        mockMvc.perform(get("/files/{fileId}", fileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void internalDelete_returnsNoContent() throws Exception {
        UUID fileId = UUID.randomUUID();

        mockMvc.perform(delete("/internal/files/{fileId}", fileId))
                .andExpect(status().isNoContent());

        verify(fileStoreService).delete(fileId);
    }
}
