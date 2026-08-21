package io.github.kaltz.feldbuch.rag.dto;

import io.github.kaltz.feldbuch.rag.model.RagAnswerResult;
import io.github.kaltz.feldbuch.rag.model.RagSource;

import java.util.List;

/**
 * 외부 API 응답 DTO
 *
 * @param answer
 * @param sources
 */
public record RagAnswerResponse(
        String answer,
        List<RagSource> sources
) {

    public static RagAnswerResponse from(RagAnswerResult result) {

        return new RagAnswerResponse(
                result.response().content(),
                result.sources()
        );
    }
}
