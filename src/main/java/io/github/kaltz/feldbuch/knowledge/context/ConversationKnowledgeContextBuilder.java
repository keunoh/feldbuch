package io.github.kaltz.feldbuch.knowledge.context;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.reader.ConversationMessageReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationKnowledgeContextBuilder {

    private final ConversationMessageReader messageReader;

    public String build(Long userId, Long conversationId) {

        List<ConversationMessage> messages =
                messageReader.findAll(
                        userId,
                        conversationId
                );

        StringBuilder builder =
                new StringBuilder();

        for (ConversationMessage message : messages) {

            builder.append(role(message))
                    .append(System.lineSeparator())
                    .append(message.getContent())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return builder.toString().trim();
    }

    private String role(ConversationMessage message) {

        return switch (message.getRole()) {
            case USER -> "USER:";
            case ASSISTANT -> "AI:";
        };
    }
}
