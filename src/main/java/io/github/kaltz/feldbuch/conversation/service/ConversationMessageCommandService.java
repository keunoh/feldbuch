package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationMessageCommandService {

    private final ConversationReader conversationReader;

    private final ConversationMessageRepository repository;

    public Long createUserMessage(Long userId, Long conversationId, String content) {

        return createMessage(
                userId,
                conversationId,
                ConversationRole.USER,
                content
        );
    }

    public Long createAssistantMessage(Long userId, Long conversationId, String content) {

        return createMessage(
                userId,
                conversationId,
                ConversationRole.ASSISTANT,
                content
        );
    }

    private Long createMessage(Long userId, Long conversationId, ConversationRole role, String content) {
        Conversation conversation = conversationReader.get(userId, conversationId);

        int sequence = repository.nextSequence(conversationId);

        ConversationMessage message =
                ConversationMessage.create(
                        conversation,
                        sequence,
                        role,
                        content
                );

        repository.save(message);

        return message.getId();
    }
}
