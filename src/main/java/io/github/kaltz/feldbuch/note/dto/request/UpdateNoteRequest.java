package io.github.kaltz.feldbuch.note.dto.request;

import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateNoteRequest(

        @NotBlank
        String title,

        @NotBlank
        String content,

        @NotNull
        NoteCategory category
) {
}
