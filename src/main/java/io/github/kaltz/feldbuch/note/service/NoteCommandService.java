package io.github.kaltz.feldbuch.note.service;

import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdatePinRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdateStudyStatusRequest;
import io.github.kaltz.feldbuch.note.dto.response.NoteResponse;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.mapper.NoteMapper;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import io.github.kaltz.feldbuch.note.repository.NoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteCommandService {

    // C, U, D 담당

    private final NoteRepository noteRepository;

    private final UserReader userReader;

    private final NoteReader noteReader;

    public NoteResponse create(
            Long userId,
            CreateNoteRequest request
    ) {
        User user = userReader.get(userId);

        Note note = Note.create(
                user,
                request.title(),
                request.content(),
                request.category()
        );

        Note savedNote = noteRepository.save(note);

        return NoteMapper.toResponse(savedNote);
    }

    public NoteResponse update(
            Long userId,
            Long noteId,
            UpdateNoteRequest request
    ) {

        Note note = noteReader.get(userId, noteId);

        note.update(
                request.title(),
                request.content(),
                request.category()
        );

        return NoteMapper.toResponse(note);
    }

    public void delete(
            Long userId,
            Long noteId
    ) {

        noteRepository.delete(
                noteReader.get(userId, noteId)
        );
    }

    public void changePinned(
            Long userId,
            Long noteId,
            UpdatePinRequest request
    ) {
        Note note = noteReader.get(userId, noteId);
        note.changePinned(request.pinned());
    }

    public void changeStudyStatus(
            Long userId,
            Long noteId,
            UpdateStudyStatusRequest request
    ) {
        Note note = noteReader.get(
                userId,
                noteId
        );

        note.changeStudyStatus(
                request.studyStatus()
        );
    }
}
