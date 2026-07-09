package io.github.kaltz.feldbuch.ai.job.dto;

import io.github.kaltz.feldbuch.ai.job.entity.AiJobStatus;
import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;

import java.time.LocalDateTime;

public record AiJobResponse(
        Long jobId,
        Long noteId,
        AiJobType type,
        AiJobStatus status,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}
