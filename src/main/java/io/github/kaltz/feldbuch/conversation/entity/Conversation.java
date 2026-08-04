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

    /**
     * 마지막으로 사용자 또는 AI 메시지가 저장된 시각
     * <p>
     * 일정 시간 동안 새 메시지가 없으면
     * 대화를 자동 완료하는 기준으로 사용한다.
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * 지식 추출이 완료된 마지막 메시지 ID
     * <p>
     * null이면 아직 한 번도 지식 추출을 하지 않은 상태다.
     */
    @Column(name = "last_extracted_message_id")
    private Long lastExtractedMessageId;

    @Builder
    private Conversation(
            User user,
            String title,
            ConversationStatus status,
            KnowledgeExtractStatus knowledgeExtractStatus,
            int knowledgeExtractRetryCount,
            String knowledgeExtractErrorMessage,
            LocalDateTime knowledgeExtractFailedAt,
            LocalDateTime lastMessageAt,
            Long lastExtractedMessageId
    ) {
        this.user = user;
        this.title = title;
        this.status = status;
        this.knowledgeExtractStatus = knowledgeExtractStatus;
        this.knowledgeExtractRetryCount = knowledgeExtractRetryCount;
        this.knowledgeExtractErrorMessage = knowledgeExtractErrorMessage;
        this.knowledgeExtractFailedAt = knowledgeExtractFailedAt;
        this.lastMessageAt = lastMessageAt;
        this.lastExtractedMessageId = lastExtractedMessageId;
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
                .lastMessageAt(null)
                .lastExtractedMessageId(null)
                .build();
    }

    public void complete() {
        if (this.status == ConversationStatus.COMPLETED) {
            return;
        }

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

    public void completeKnowledgeExtraction(
            Long extractedThroughMessageId
    ) {
        validateExtractionCheckpoint(
                extractedThroughMessageId
        );

        changeKnowledgeExtractStatus(
                KnowledgeExtractStatus.COMPLETED
        );

        this.lastExtractedMessageId =
                extractedThroughMessageId;

        this.knowledgeExtractRetryCount = 0;
        this.knowledgeExtractErrorMessage = null;
        this.knowledgeExtractFailedAt = null;
    }

    public void failKnowledgeExtraction(String errorMessage, LocalDateTime failedAt) {
        if (failedAt == null) {
            throw new IllegalArgumentException(
                    "지식 추출 실패 시각은 필수입니다."
            );
        }

        changeKnowledgeExtractStatus(KnowledgeExtractStatus.FAILED);

        this.knowledgeExtractRetryCount++;

        this.knowledgeExtractErrorMessage =
                normalizeErrorMessage(errorMessage);

        this.knowledgeExtractFailedAt =
                failedAt;
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

    public void recordMessageActivity(
            LocalDateTime messageAt
    ) {
        if (messageAt == null) {
            throw new IllegalArgumentException(
                    "메시지 활동 시각은 필수입니다."
            );
        }

        boolean wasCompleted =
                this.status == ConversationStatus.COMPLETED;

        this.status = ConversationStatus.ACTIVE;
        this.lastMessageAt = messageAt;

        /*
         * 완료 후 다시 시작된 대화라면
         * 마지막 체크포인트 이후 메시지를 새로 추출할 수 있도록 한다.
         *
         * lastExtractedMessageId는 지우지 않는다.
         */
        if (wasCompleted) {
            prepareNextKnowledgeExtraction();
        }
    }

    private void prepareNextKnowledgeExtraction() {
        changeKnowledgeExtractStatus(
                KnowledgeExtractStatus.NONE
        );

        this.knowledgeExtractRetryCount = 0;
        this.knowledgeExtractErrorMessage = null;
        this.knowledgeExtractFailedAt = null;
    }

    private void validateExtractionCheckpoint(
            Long extractedThroughMessageId
    ) {
        if (extractedThroughMessageId == null) {
            throw new IllegalArgumentException(
                    "마지막 추출 메시지 ID는 필수입니다."
            );
        }

        if (
                this.lastExtractedMessageId != null
                        && extractedThroughMessageId
                        < this.lastExtractedMessageId
        ) {
            throw new IllegalArgumentException(
                    "지식 추출 체크포인트는 이전 값보다 작을 수 없습니다."
            );
        }
    }


    public boolean isActive() {
        return this.status == ConversationStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return this.status == ConversationStatus.COMPLETED;
    }
}
