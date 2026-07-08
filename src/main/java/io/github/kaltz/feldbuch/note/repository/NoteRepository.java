package io.github.kaltz.feldbuch.note.repository;

import io.github.kaltz.feldbuch.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByUserIdOrderByPinnedDescCreatedAtDesc(Long userId);

    Optional<Note> findByIdAndUserId(Long noteId, Long userId);
}
