package io.github.kaltz.feldbuch.ai.job.service;

import io.github.kaltz.feldbuch.ai.job.dto.AiJobResponse;
import io.github.kaltz.feldbuch.ai.job.mapper.AiJobMapper;
import io.github.kaltz.feldbuch.ai.job.reader.AiJobReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiJobQueryService {

    private final AiJobReader aiJobReader;

    public AiJobResponse findById(Long jobId) {
        return AiJobMapper.toResponse(aiJobReader.get(jobId));
    }
}
