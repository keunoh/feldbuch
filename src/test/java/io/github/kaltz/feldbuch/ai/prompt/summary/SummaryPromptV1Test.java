package io.github.kaltz.feldbuch.ai.prompt.summary;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryPromptV1Test {

    @Test
    @DisplayName("요약 프롬프트 생성")
    void createPrompt() {

        SummaryRequest request = new SummaryRequest(
                "Spring Boot",
                "Spring Boot는 백엔드 프레임워크이다.",
                "STUDY",
                "TODO"
        );

        SummaryPromptV1 prompt = new SummaryPromptV1();
        String result = prompt.create(request);

        assertThat(result).contains("Spring Boot");
        assertThat(result).contains("STUDY");
        assertThat(result).contains("TODO");
        assertThat(result).contains("Spring Boot는 백엔드 프레임워크이다.");
    }

}