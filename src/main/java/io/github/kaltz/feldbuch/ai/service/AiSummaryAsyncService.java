package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.mapper.SummaryMapper;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSummaryAsyncService {

    private final NoteReader noteReader;
    private final SummaryService summaryService;

    @Async
    @Transactional
    public void summarize(
            Long userId,
            Long noteId
    ) {

        // 새로운 스레드
        // 새로운 트랜잭션
        // 다시 Note 조회
        Note note = noteReader.get(userId, noteId);

        SummaryRequest request = SummaryMapper.toRequest(note);

        String summary = summaryService.summarize(request);

        note.changeSummary(summary);
    }
}
