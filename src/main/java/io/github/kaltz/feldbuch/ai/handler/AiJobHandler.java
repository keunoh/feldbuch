package io.github.kaltz.feldbuch.ai.handler;

import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;

public interface AiJobHandler {

    AiJobType support();

    void execute(Long jobId, Long userId, Long noteId);
}
