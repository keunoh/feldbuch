package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.job.service.AiJobService;
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
    private final AiJobService aiJobService;

    @Async
    @Transactional
    public void summarize(Long jobId, Long userId, Long noteId) {

        // Job 시작
        aiJobService.start(jobId);

        Note note = noteReader.get(userId, noteId);
        // 새로운 스레드
        // 새로운 트랜잭션
        // 다시 Note 조회
        try {
            SummaryRequest request = SummaryMapper.toRequest(note);

            String summary = summaryService.summarize(request);

            note.completeSummary(summary);

            log.info("AI 요약 완료. jobId={}, noteId={}", jobId, noteId);

        } catch (Exception e) {

            note.changeSummaryStatusFailed();

            aiJobService.fail(jobId, e.getMessage());

            // 로그는 반드시 남긴다.
            log.error("AI 요약 실패, jobId={}, noteId={}", jobId, noteId, e);
        }
    }
}
