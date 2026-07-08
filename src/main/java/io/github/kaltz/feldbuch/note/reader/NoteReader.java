package io.github.kaltz.feldbuch.note.reader;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteReader {

    private final NoteRepository noteRepository;

    public Note get(Long userId, Long noteId) {
        return noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOTE_NOT_FOUND));
    }

    public Note get(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOTE_NOT_FOUND));
    }
}
