package io.github.kaltz.feldbuch.conversation.reader;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationReader {

    private final ConversationRepository repository;

    public Conversation get(Long userId, Long conversationId) {
        return repository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND)
                );
    }

    public List<Conversation> findAll(Long userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
