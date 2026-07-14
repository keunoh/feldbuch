package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.dto.response.ConversationResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationQueryService {

    private final ConversationReader conversationReader;

    public ConversationResponse findById(Long conversationId) {

        Conversation conversation = conversationReader.get(conversationId);

        return ConversationResponse.from(conversation);
    }
}
