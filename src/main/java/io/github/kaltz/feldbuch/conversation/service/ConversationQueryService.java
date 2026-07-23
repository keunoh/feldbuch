package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.dto.response.ConversationResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationQueryService {

    private final ConversationReader conversationReader;

    public ConversationResponse findById(Long userId, Long conversationId) {

        Conversation conversation = conversationReader.get(userId, conversationId);

        return ConversationResponse.from(conversation);
    }

    public List<ConversationResponse> findAll(Long userId) {

        return conversationReader.findAll(userId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

}
