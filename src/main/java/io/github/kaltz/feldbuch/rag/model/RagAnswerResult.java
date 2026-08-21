package io.github.kaltz.feldbuch.rag.model;

import io.github.kaltz.feldbuch.ai.model.ChatResponse;

import java.util.List;

/**
 * 서비스 계층의 결과 모델
 *
 * @param response
 * @param sources
 */
public record RagAnswerResult(
        ChatResponse response,
        List<RagSource> sources
) {
}
