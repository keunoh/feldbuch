package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KnowledgeExtractionStatusService {

    private final ConversationRepository conversationRepository;
    private final Clock clock;

    /**
     * 지식 추출 작업 시작 상태로 변경한다.
     * <p>
     * 배치 Step의 트랜잭션과 분리하여
     * PROCESSING 상태를 즉시 반영한다.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void start(
            Long conversationId
    ) {
        Conversation conversation =
                getConversation(conversationId);

        conversation.startKnowledgeExtraction();
    }

    /**
     * 지식 추출 실패 상태로 변경한다.
     * <p>
     * 원래 추출 트랜잭션이 롤백된 뒤에도
     * 실패 상태를 별도로 기록한다.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void fail(
            Long conversationId,
            String errorMessage
    ) {
        Conversation conversation =
                getConversation(conversationId);

        conversation.failKnowledgeExtraction(
                errorMessage,
                LocalDateTime.now(clock)
        );
    }

    private Conversation getConversation(
            Long conversationId
    ) {
        return conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "대화를 찾을 수 없습니다. conversationId="
                                        + conversationId
                        )
                );
    }
}
