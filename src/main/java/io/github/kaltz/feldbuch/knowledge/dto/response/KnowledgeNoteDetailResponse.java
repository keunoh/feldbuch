package io.github.kaltz.feldbuch.knowledge.dto.response;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

import java.util.List;

public record KnowledgeNoteDetailResponse(
        Long id,
        String title,
        String description,
        String summary,
        List<String> keywords
) {

    public static KnowledgeNoteDetailResponse from(KnowledgeNote note) {
        return new KnowledgeNoteDetailResponse(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.getSummary(),
                note.getKeywords()
        );
    }
}
