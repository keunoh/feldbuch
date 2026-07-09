package io.github.kaltz.feldbuch.ai.job.reader;

import io.github.kaltz.feldbuch.ai.job.entity.AiJob;
import io.github.kaltz.feldbuch.ai.job.repository.AiJobRepository;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiJobReader {

    private final AiJobRepository repository;

    public AiJob get(Long jobId) {
        return repository.findById(jobId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.AI_JOB_NOT_FOUND
                ));
    }
}
