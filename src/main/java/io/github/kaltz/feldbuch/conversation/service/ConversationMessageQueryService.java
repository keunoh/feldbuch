package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.dto.response.ConversationMessageResponse;
import io.github.kaltz.feldbuch.conversation.reader.ConversationMessageReader;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationMessageQueryService {

    private final ConversationReader conversationReader;
    private final ConversationMessageReader messageReader;

    public List<ConversationMessageResponse> findAll(Long userId, Long conversationId) {

        conversationReader.get(userId, conversationId);

        return messageReader
                .findAll(userId, conversationId)
                .stream()
                .map(ConversationMessageResponse::from)
                .toList();
    }
}
