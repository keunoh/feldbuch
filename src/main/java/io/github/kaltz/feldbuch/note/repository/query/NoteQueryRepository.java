package io.github.kaltz.feldbuch.note.repository.query;

import io.github.kaltz.feldbuch.note.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteQueryRepository {

    Page<Note> search(
            Long userId,
            String keyword,
            Pageable pageable
    );
}
