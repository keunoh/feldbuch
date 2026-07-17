package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.dto.request.CreateConversationMessageRequest;
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

    public Long create(Long userId, Long conversationId, CreateConversationMessageRequest request) {

        Conversation conversation = conversationReader.get(userId, conversationId);

        int sequence = repository.nextSequence(conversationId);

        ConversationMessage message = ConversationMessage.create(
                conversation,
                sequence,
                ConversationRole.USER,
                request.content()
        );

        repository.save(message);

        return message.getId();
    }
}
