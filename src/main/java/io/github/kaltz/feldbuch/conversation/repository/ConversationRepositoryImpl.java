package io.github.kaltz.feldbuch.conversation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationStatus;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.github.kaltz.feldbuch.conversation.entity.QConversation.conversation;

@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Conversation> findKnowledgeExtractionTargets() {

        return queryFactory
                .selectFrom(conversation)
                .where(
                        conversation.status.eq(
                                ConversationStatus.COMPLETED
                        ),
                        conversation.knowledgeExtractStatus.eq(
                                KnowledgeExtractStatus.NONE
                        )
                )
                .orderBy(
                        conversation.updatedAt.asc()
                )
                .fetch();
    }
}
