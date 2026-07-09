package io.github.kaltz.feldbuch.ai.job.repository;

import io.github.kaltz.feldbuch.ai.job.entity.AiJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiJobRepository extends JpaRepository<AiJob, Long> {
}
