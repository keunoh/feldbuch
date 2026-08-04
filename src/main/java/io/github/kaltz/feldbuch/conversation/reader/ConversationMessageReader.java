package io.github.kaltz.feldbuch.conversation.reader;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationMessageReader {

    private final ConversationReader conversationReader;

    private final ConversationMessageRepository
            messageRepository;

    public List<ConversationMessage> findAll(
            Long userId,
            Long conversationId
    ) {
        validateConversationOwner(
                userId,
                conversationId
        );

        return messageRepository
                .findAllByConversationIdOrderByIdAsc(
                        conversationId
                );
    }

    public List<ConversationMessage> findAfter(
            Long userId,
            Long conversationId,
            Long lastExtractedMessageId
    ) {
        validateConversationOwner(
                userId,
                conversationId
        );

        if (lastExtractedMessageId == null) {
            return messageRepository
                    .findAllByConversationIdOrderByIdAsc(
                            conversationId
                    );
        }

        return messageRepository
                .findAllByConversationIdAndIdGreaterThanOrderByIdAsc(
                        conversationId,
                        lastExtractedMessageId
                );
    }

    private void validateConversationOwner(
            Long userId,
            Long conversationId
    ) {
        conversationReader.get(
                userId,
                conversationId
        );
    }
}
