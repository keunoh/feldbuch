package io.github.kaltz.feldbuch.rag.dto;

public record RagAnswerResponse(
        String answer
) {

    public static RagAnswerResponse from(String answer) {

        return new RagAnswerResponse(answer);
    }
}
