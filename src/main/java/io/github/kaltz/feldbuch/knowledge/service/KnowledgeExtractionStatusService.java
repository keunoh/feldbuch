package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeExtractionStatusService {

    private final ConversationRepository conversationRepository;

    /**
     * 지식 추출 작업 시작 상태로 변경한다.
     * <p>
     * 배치의 외부 트랜잭션과 분리하여 즉시 반영한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(Long conversationId) {

        Conversation conversation = getConversation(conversationId);

        conversation.startKnowledgeExtraction();
    }

    /**
     * 지식 추출 작업 완료 상태로 변경한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long conversationId) {

        Conversation conversation = getConversation(conversationId);

        conversation.completeKnowledgeExtraction();
    }

    /**
     * 지식 추출 작업 실패 상태로 변경한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long conversationId, String errorMessage) {

        Conversation conversation = getConversation(conversationId);

        conversation.failKnowledgeExtraction(errorMessage);
    }

    private Conversation getConversation(Long conversationId) {

        return conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "대화를 찾을 수 없습니다. conversationId=" + conversationId
                        )
                );
    }
}
