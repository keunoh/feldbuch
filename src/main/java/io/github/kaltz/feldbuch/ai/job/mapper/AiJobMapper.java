package io.github.kaltz.feldbuch.ai.job.mapper;

import io.github.kaltz.feldbuch.ai.job.dto.AiJobResponse;
import io.github.kaltz.feldbuch.ai.job.entity.AiJob;

public class AiJobMapper {

    private AiJobMapper() {
    }

    public static AiJobResponse toResponse(AiJob job) {
        return new AiJobResponse(
                job.getId(),
                job.getNoteId(),
                job.getJobType(),
                job.getStatus(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getErrorMessage());
    }

}
