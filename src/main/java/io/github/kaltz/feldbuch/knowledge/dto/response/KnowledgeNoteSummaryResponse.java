package io.github.kaltz.feldbuch.knowledge.dto.response;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

import java.time.LocalDateTime;

public record KnowledgeNoteSummaryResponse(
        Long id,
        String title,
        String summary,
        LocalDateTime createdAt
) {

    public static KnowledgeNoteSummaryResponse from(
            KnowledgeNote note
    ) {

        return new KnowledgeNoteSummaryResponse(
                note.getId(),
                note.getTitle(),
                note.getSummary(),
                note.getCreatedAt()
        );
    }
}
