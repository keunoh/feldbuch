package io.github.kaltz.feldbuch.note.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.kaltz.feldbuch.note.entity.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoteQueryRepositoryImpl implements NoteQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Note> search(Long userId, String keyword, Pageable pageable) {
        return null;
    }
}
