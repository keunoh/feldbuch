package io.github.kaltz.feldbuch.note.dto.response;

import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.note.entity.StudyStatus;

public record NoteListResponse(
        Long id,
        String title,
        String summary,
        NoteCategory category,
        boolean pinned,
        StudyStatus studyStatus
) {
}
