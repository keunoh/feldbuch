package io.github.kaltz.feldbuch.conversation.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import io.github.kaltz.feldbuch.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation extends BaseEntity {

    public static final String DEFAULT_TITLE = "새 대화";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "knowledge_extract_status",
            nullable = false,
            length = 20
    )
    private KnowledgeExtractStatus knowledgeExtractStatus = KnowledgeExtractStatus.NONE;

    @Column(
            name = "knowledge_extract_retry_count",
            nullable = false
    )
    private int knowledgeExtractRetryCount;

    @Column(
            name = "knowledge_extract_error_message",
            length = 1000
    )
    private String knowledgeExtractErrorMessage;

    @Column(
            name = "knowledge_extract_failed_at"
    )
    private LocalDateTime knowledgeExtractFailedAt;

    @Builder
    private Conversation(
            User user,
            String title,
            ConversationStatus status,
            KnowledgeExtractStatus knowledgeExtractStatus,
            int knowledgeExtractRetryCount,
            String knowledgeExtractErrorMessage,
            LocalDateTime knowledgeExtractFailedAt
    ) {
        this.user = user;
        this.title = title;
        this.status = status;
        this.knowledgeExtractStatus = knowledgeExtractStatus;
        this.knowledgeExtractRetryCount = knowledgeExtractRetryCount;
        this.knowledgeExtractErrorMessage = knowledgeExtractErrorMessage;
        this.knowledgeExtractFailedAt = knowledgeExtractFailedAt;
    }

    public static Conversation create(User user) {
        return create(user, DEFAULT_TITLE);
    }

    public static Conversation create(User user, String title) {
        return Conversation.builder()
                .user(user)
                .title(title)
                .status(ConversationStatus.ACTIVE)
                .knowledgeExtractStatus(KnowledgeExtractStatus.NONE)
                .knowledgeExtractRetryCount(0)
                .knowledgeExtractErrorMessage(null)
                .knowledgeExtractFailedAt(null)
                .build();
    }

    public void complete() {
        this.status = ConversationStatus.COMPLETED;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public boolean hasDefaultTitle() {
        return DEFAULT_TITLE.equals(title);
    }

    public void touch() {
        super.touch();
    }

    private void changeKnowledgeExtractStatus(KnowledgeExtractStatus status) {
        this.knowledgeExtractStatus = status;
    }

    public void startKnowledgeExtraction() {
        changeKnowledgeExtractStatus(KnowledgeExtractStatus.PROCESSING);

        this.knowledgeExtractErrorMessage = null;
    }

    public void completeKnowledgeExtraction() {
        changeKnowledgeExtractStatus(KnowledgeExtractStatus.COMPLETED);

        this.knowledgeExtractErrorMessage = null;
        this.knowledgeExtractFailedAt = null;
    }

    public void failKnowledgeExtraction(String errorMessage) {
        changeKnowledgeExtractStatus(KnowledgeExtractStatus.FAILED);

        this.knowledgeExtractRetryCount++;

        this.knowledgeExtractErrorMessage =
                normalizeErrorMessage(errorMessage);

        this.knowledgeExtractFailedAt =
                LocalDateTime.now();
    }

    private String normalizeErrorMessage(
            String errorMessage
    ) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "알 수 없는 오류";
        }

        String normalized = errorMessage.trim();

        if (normalized.length() <= 1000) {
            return normalized;
        }

        return normalized.substring(0, 1000);
    }

    public void resetKnowledgeExtraction() {
        this.knowledgeExtractStatus =
                KnowledgeExtractStatus.NONE;

        this.knowledgeExtractErrorMessage = null;
        this.knowledgeExtractFailedAt = null;
    }
}
