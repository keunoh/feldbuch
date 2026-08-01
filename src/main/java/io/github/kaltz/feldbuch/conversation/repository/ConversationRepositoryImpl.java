package io.github.kaltz.feldbuch.conversation.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationStatus;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static io.github.kaltz.feldbuch.conversation.entity.QConversation.conversation;

@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MINUTES = 1L;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Conversation> findKnowledgeExtractionTargets() {

        LocalDateTime retryAvailableBefore =
                LocalDateTime.now()
                        .minusMinutes(
                                RETRY_DELAY_MINUTES
                        );

        return queryFactory
                .selectFrom(conversation)
                .where(
                        conversation.status.eq(
                                ConversationStatus.COMPLETED
                        ),
                        pendingOrRetryable(
                                retryAvailableBefore
                        )
                )
                .orderBy(
                        conversation.updatedAt.asc()
                )
                .fetch();
    }

    private BooleanExpression pendingOrRetryable(LocalDateTime retryAvailableBefore) {

        BooleanExpression pending =
                conversation
                        .knowledgeExtractStatus
                        .eq(KnowledgeExtractStatus.NONE);

        BooleanExpression retryableFailure =
                conversation
                        .knowledgeExtractStatus
                        .eq(KnowledgeExtractStatus.FAILED)
                        .and(
                                conversation
                                        .knowledgeExtractRetryCount
                                        .lt(MAX_RETRY_COUNT)
                        )
                        .and(
                                conversation
                                        .knowledgeExtractFailedAt
                                        .loe(retryAvailableBefore)
                        );

        return pending.or(retryableFailure);
    }
}
