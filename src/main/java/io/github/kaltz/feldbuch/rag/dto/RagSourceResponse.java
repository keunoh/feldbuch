package io.github.kaltz.feldbuch.rag.dto;

import io.github.kaltz.feldbuch.rag.model.RagSource;

public record RagSourceResponse(
        Long knowledgeNoteId,
        Long knowledgeId,
        Long conversationId
) {

    public static RagSourceResponse from(RagSource source) {

        return new RagSourceResponse(
                source.knowledgeNoteId(),
                source.knowledgeId(),
                source.conversationId()
        );
    }
}
