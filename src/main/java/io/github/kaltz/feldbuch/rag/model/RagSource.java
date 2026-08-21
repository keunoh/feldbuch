package io.github.kaltz.feldbuch.rag.model;

public record RagSource(
        Long knowledgeNoteId,
        Long knowledgeId,
        Long conversationId,
        Double score
) {
}
