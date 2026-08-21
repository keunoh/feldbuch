package io.github.kaltz.feldbuch.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagAnswerRequest(
        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 2000, message = "질문은 2000자 이하입니다.")
        String question
) {
}
