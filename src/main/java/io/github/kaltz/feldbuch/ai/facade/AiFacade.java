package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.service.AiSummaryAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiFacade {

    private final AiSummaryAsyncService aiSummaryAsyncService;

    public void summarize(
            Long userId,
            Long noteId
    ) {
        aiSummaryAsyncService.summarize(userId, noteId);
    }
}
