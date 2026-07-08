package io.github.kaltz.feldbuch.note.service;

import io.github.kaltz.feldbuch.note.dto.response.NoteListResponse;
import io.github.kaltz.feldbuch.note.dto.response.NoteResponse;
import io.github.kaltz.feldbuch.note.mapper.NoteMapper;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import io.github.kaltz.feldbuch.note.repository.query.NoteQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteQueryService {

    // R 담당

    private final NoteQueryRepository noteQueryRepository;
    private final NoteReader noteReader;

    @Transactional(readOnly = true)
    public NoteResponse findById(Long userId, Long noteId) {

        return NoteMapper.toResponse(
                noteReader.get(userId, noteId)
        );
    }

    @Transactional(readOnly = true)
    public Page<NoteListResponse> search(
            Long userId,
            String keyword,
            Pageable pageable) {

        return noteQueryRepository
                .search(
                        userId,
                        keyword,
                        pageable
                )
                .map(NoteMapper::toListResponse);
    }
}
