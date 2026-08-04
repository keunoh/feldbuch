package io.github.kaltz.feldbuch.conversation.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationStatus;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.kaltz.feldbuch.conversation.entity.QConversation.conversation;

@RequiredArgsConstructor
public class ConversationRepositoryImpl
        implements ConversationRepositoryCustom {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MINUTES = 1L;

    private final JPAQueryFactory queryFactory;
    private final Clock clock;

    @Override
    public List<Conversation> findKnowledgeExtractionTargets() {
        return queryFactory
                .selectFrom(conversation)
                .where(
                        knowledgeExtractionTargetCondition()
                )
                .orderBy(
                        conversation.updatedAt.asc()
                )
                .fetch();
    }

    @Override
    public boolean existsKnowledgeExtractionTarget() {
        Integer result =
                queryFactory
                        .selectOne()
                        .from(conversation)
                        .where(
                                knowledgeExtractionTargetCondition()
                        )
                        .fetchFirst();

        return result != null;
    }

    @Override
    public List<Conversation> findInactiveActiveConversations(
            LocalDateTime cutoff
    ) {
        if (cutoff == null) {
            throw new IllegalArgumentException(
                    "자동 완료 기준 시각은 필수입니다."
            );
        }

        return queryFactory
                .selectFrom(conversation)
                .where(
                        conversation.status.eq(
                                ConversationStatus.ACTIVE
                        ),
                        conversation.lastMessageAt.isNotNull(),
                        conversation.lastMessageAt.loe(
                                cutoff
                        )
                )
                .orderBy(
                        conversation.lastMessageAt.asc()
                )
                .fetch();
    }

    private BooleanExpression
    knowledgeExtractionTargetCondition() {
        LocalDateTime retryAvailableBefore =
                LocalDateTime.now(clock)
                        .minusMinutes(
                                RETRY_DELAY_MINUTES
                        );

        return conversation.status
                .eq(ConversationStatus.COMPLETED)
                .and(
                        pendingOrRetryable(
                                retryAvailableBefore
                        )
                );
    }

    private BooleanExpression pendingOrRetryable(
            LocalDateTime retryAvailableBefore
    ) {
        BooleanExpression pending =
                conversation
                        .knowledgeExtractStatus
                        .eq(
                                KnowledgeExtractStatus.NONE
                        );

        BooleanExpression retryableFailure =
                conversation
                        .knowledgeExtractStatus
                        .eq(
                                KnowledgeExtractStatus.FAILED
                        )
                        .and(
                                conversation
                                        .knowledgeExtractRetryCount
                                        .lt(
                                                MAX_RETRY_COUNT
                                        )
                        )
                        .and(
                                conversation
                                        .knowledgeExtractFailedAt
                                        .loe(
                                                retryAvailableBefore
                                        )
                        );

        return pending.or(
                retryableFailure
        );
    }
}
