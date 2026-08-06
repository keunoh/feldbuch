package io.github.kaltz.feldbuch.ai.dto;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;

import java.util.List;

public record AiKnowledgeMergeResponse(
        KnowledgeCategory category,
        String title,
        String description,
        String summary,
        List<String> keywords
) {
}