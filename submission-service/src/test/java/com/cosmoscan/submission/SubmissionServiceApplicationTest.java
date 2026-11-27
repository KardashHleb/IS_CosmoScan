package com.cosmoscan.submission;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SubmissionServiceApplicationTest {

    @Test
    void main_runsSpringApplication() {
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            SubmissionServiceApplication.main(args);

            spring.verify(() -> SpringApplication.run(SubmissionServiceApplication.class, args));
        }
    }

    @Test
    void applicationClass_canBeInstantiated() {
        assertThat(new SubmissionServiceApplication()).isNotNull();
    }
}
