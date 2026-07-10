package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.handler.AiJobHandlerFactory;
import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;
import io.github.kaltz.feldbuch.ai.job.service.AiJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiFacade {

    private final AiJobHandlerFactory handlerFactory;

    private final AiJobService aiJobService;

    public Long summarize(Long userId, Long noteId) {
        // 1. AI Job 생성
        Long jobId = aiJobService.request(noteId, AiJobType.SUMMARY);

        // 2. 비동기 실행
        handlerFactory
                .get(AiJobType.SUMMARY)
                .execute(jobId, userId, noteId);

        // 3. JobId 반환
        return jobId;
    }
}
