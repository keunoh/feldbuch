package io.github.kaltz.feldbuch.knowledge.folder;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;

public record KnowledgeFolderCandidate(
        Long id,
        String name
) {
    public static KnowledgeFolderCandidate from(
            Knowledge knowledge
    ) {
        if (knowledge == null) {
            throw new IllegalArgumentException(
                    "Knowledge는 필수입니다."
            );
        }

        return new KnowledgeFolderCandidate(
                knowledge.getId(),
                knowledge.getName()
        );
    }
}
