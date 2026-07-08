package io.github.kaltz.feldbuch.ai.dto;

import io.github.kaltz.feldbuch.note.entity.Note;

public record SummaryRequest(
        String title,
        String content,
        String category,
        String studyStatus
) {

    public static SummaryRequest from(Note note) {
        return new SummaryRequest(
                note.getTitle(),
                note.getContent(),
                note.getCategory().name(),
                note.getStudyStatus().name()
        );
    }
}
