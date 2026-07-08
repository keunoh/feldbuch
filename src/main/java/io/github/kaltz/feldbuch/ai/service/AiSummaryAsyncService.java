package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.entity.AiSummaryStatus;
import io.github.kaltz.feldbuch.ai.mapper.SummaryMapper;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSummaryAsyncService {

    private final NoteReader noteReader;
    private final SummaryService summaryService;

    @Async
    @Transactional
    public void summarize(
            Long noteId
    ) {

        Note note = noteReader.get(noteId);
        // 새로운 스레드
        // 새로운 트랜잭션
        // 다시 Note 조회
        try {
            SummaryRequest request = SummaryMapper.toRequest(note);

            String summary = summaryService.summarize(request);

            note.changeSummary(summary);
        } catch (Exception e) {
            note.changeSummaryStatus(
                    AiSummaryStatus.FAILED
            );

            // 로그는 반드시 남긴다.
            log.error(
                    "AI 요약 실패, noteId={}",
                    noteId,
                    e
            );
        }
    }
}
