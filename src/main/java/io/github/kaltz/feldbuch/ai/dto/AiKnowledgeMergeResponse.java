package io.github.kaltz.feldbuch.ai.dto;

import java.util.List;

public record AiKnowledgeMergeResponse(
        List<String> knowledgePath,
        String title,
        String description,
        String summary,
        List<String> keywords
) {
}