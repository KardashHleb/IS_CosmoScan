package com.cosmoscan.submission.api;

import com.cosmoscan.submission.service.WorkSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Runs with the real Spring MVC setup so that method validation (@Validated + @NotBlank) is applied. */
@WebMvcTest(WorkController.class)
class WorkControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkSubmissionService workSubmissionService;

    @Test
    void submitWork_blankStudentName_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "essay.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/works").file(file).param("studentFullName", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("studentFullName: must not be blank"));

        verifyNoInteractions(workSubmissionService);
    }

    @Test
    void submitWork_missingStudentName_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "essay.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/works").file(file))
                .andExpect(status().isBadRequest());
    }
}
