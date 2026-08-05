package io.github.kaltz.feldbuch.ai.dto;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;

import java.util.List;

public record AiKnowledgeMergeResponse(
        KnowledgeRootCategory rootCategory,
        List<String> knowledgePath,
        String title,
        String description,
        String summary,
        List<String> keywords
) {
}
