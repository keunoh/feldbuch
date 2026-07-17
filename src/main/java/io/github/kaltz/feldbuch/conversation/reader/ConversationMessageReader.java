package io.github.kaltz.feldbuch.conversation.reader;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationMessageReader {

    private final ConversationMessageRepository repository;

    public List<ConversationMessage> getMessages(Long conversationId) {

        return repository.findAllByConversationIdOrderBySequenceAsc(conversationId);
    }
}
