package io.github.kaltz.feldbuch.knowledge.dto.response;

import java.util.List;

public record KnowledgeTreeResponse(
        Long id,
        String name,
        List<KnowledgeTreeResponse> children
) {
}
