package io.github.kaltz.feldbuch.note.dto.response;

import io.github.kaltz.feldbuch.note.entity.NoteCategory;

public record NoteResponse(
        Long id,
        String title,
        String content,
        String summary,
        NoteCategory category,
        boolean pinned
) {
}
