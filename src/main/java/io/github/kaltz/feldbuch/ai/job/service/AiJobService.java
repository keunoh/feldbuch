package io.github.kaltz.feldbuch.ai.job.service;

import io.github.kaltz.feldbuch.ai.job.entity.AiJob;
import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;
import io.github.kaltz.feldbuch.ai.job.reader.AiJobReader;
import io.github.kaltz.feldbuch.ai.job.repository.AiJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiJobService {

    private final AiJobRepository repository;

    private final AiJobReader reader;

    public Long request(Long noteId, AiJobType jobType) {
        AiJob job = repository.save(AiJob.create(noteId, jobType));

        return job.getId();
    }

    public void start(Long jobId) {
        reader.get(jobId).start();
    }

    public void complete(Long jobId) {
        reader.get(jobId).complete();
    }

    public void fail(Long jobId, String message) {
        reader.get(jobId).fail(message);
    }
}
