package io.github.kaltz.feldbuch.conversation.repository;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    Optional<ConversationMessage> findTopByConversationIdOrderBySequenceDesc(Long conversationId);

    List<ConversationMessage> findAllByConversationIdOrderBySequenceAsc(Long conversationId);

    default int nextSequence(Long conversationId) {
        return findTopByConversationIdOrderBySequenceDesc(conversationId)
                .map(message -> message.getSequence() + 1)
                .orElse(1);
    }
}
