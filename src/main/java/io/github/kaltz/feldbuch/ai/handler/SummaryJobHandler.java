package io.github.kaltz.feldbuch.ai.handler;

import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;
import io.github.kaltz.feldbuch.ai.service.AiSummaryAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryJobHandler implements AiJobHandler {

    private final AiSummaryAsyncService asyncService;

    @Override
    public AiJobType support() {
        return AiJobType.SUMMARY;
    }

    @Override
    public void execute(Long jobId, Long userId, Long noteId) {
        asyncService.summarize(jobId, userId, noteId);
    }
}
