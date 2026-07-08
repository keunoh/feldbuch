package io.github.kaltz.feldbuch.note.mapper;

import io.github.kaltz.feldbuch.note.dto.response.NoteListResponse;
import io.github.kaltz.feldbuch.note.dto.response.NoteResponse;
import io.github.kaltz.feldbuch.note.entity.Note;

public final class NoteMapper {

    private NoteMapper() {

    }

    public static NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getSummary(),
                note.getCategory(),
                note.isPinned(),
                note.getStudyStatus()
        );
    }

    public static NoteListResponse toListResponse(Note note) {
        return new NoteListResponse(
                note.getId(),
                note.getTitle(),
                note.getSummary(),
                note.getCategory(),
                note.isPinned(),
                note.getStudyStatus()
        );
    }
}
