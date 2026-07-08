package io.github.kaltz.feldbuch.note.dto.response;

import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.note.entity.StudyStatus;

public record NoteResponse(
        Long id,
        String title,
        String content,
        String summary,
        NoteCategory category,
        boolean pinned,
        StudyStatus studyStatus
) {
}
