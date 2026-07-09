package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;
import io.github.kaltz.feldbuch.ai.job.service.AiJobService;
import io.github.kaltz.feldbuch.ai.service.AiSummaryAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiFacade {

    private final AiSummaryAsyncService aiSummaryAsyncService;

    private final AiJobService aiJobService;

    public Long summarize(Long userId, Long noteId) {
        // 1. AI Job 생성
        Long jobId = aiJobService.request(noteId, AiJobType.SUMMARY);

        // 2. 비동기 실행
        aiSummaryAsyncService.summarize(jobId, userId, noteId);

        // 3. JobId 반환
        return jobId;
    }
}
