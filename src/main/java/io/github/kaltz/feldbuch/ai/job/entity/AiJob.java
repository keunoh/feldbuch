package io.github.kaltz.feldbuch.ai.job.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long noteId;

    @Enumerated(EnumType.STRING)
    private AiJobType jobType;

    @Enumerated(EnumType.STRING)
    private AiJobStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String errorMessage;

    @Builder
    private AiJob(Long noteId, AiJobType jobType) {
        this.noteId = noteId;
        this.jobType = jobType;
        this.status = AiJobStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public static AiJob create(Long noteId, AiJobType jobType) {
        return AiJob.builder()
                .noteId(noteId)
                .jobType(jobType)
                .build();
    }

    public void start() {
        this.status = AiJobStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = AiJobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = AiJobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
