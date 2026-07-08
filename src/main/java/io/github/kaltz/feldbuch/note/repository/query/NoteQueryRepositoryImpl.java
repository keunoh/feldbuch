package io.github.kaltz.feldbuch.note.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.entity.QNote;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoteQueryRepositoryImpl implements NoteQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Note> search(Long userId, String keyword, Pageable pageable) {

        QNote note = QNote.note;

        List<Note> content = queryFactory
                .selectFrom(note)
                .where(
                        userIdEq(userId),
                        keywordContains(keyword)
                )
                .orderBy(
                        note.pinned.desc(),
                        note.createdAt.desc(),
                        note.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(note.count())
                .from(note)
                .where(
                        userIdEq(userId),
                        keywordContains(keyword)
                );

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                countQuery::fetchOne
        );
    }

    private BooleanExpression userIdEq(Long userId) {
        return QNote.note.user.id.eq(userId);
    }

    private BooleanExpression keywordContains(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QNote.note.title.containsIgnoreCase(keyword)
                .or(
                        QNote.note.content.containsIgnoreCase(keyword)
                );
    }
}
