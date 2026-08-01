package io.github.kaltz.feldbuch.conversation.reader;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeConversationReader {

    private final ConversationRepository conversationRepository;

    public List<Conversation> findExtractionTargets() {

        return conversationRepository
                .findKnowledgeExtractionTargets();
    }
}
