package io.github.kaltz.feldbuch.knowledge.context;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;

import java.util.List;

public record ConversationAiContext(
        List<ConversationMessage> messages,
        String content
) {

    public ConversationAiContext {
        messages =
                messages == null
                        ? List.of()
                        : List.copyOf(messages);

        content =
                content == null
                        ? ""
                        : content;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Long firstMessageId() {
        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(0)
                .getId();
    }

    public Long lastMessageId() {
        if (messages.isEmpty()) {
            return null;
        }

        return messages
                .get(messages.size() - 1)
                .getId();
    }

    public int messageCount() {
        return messages.size();
    }
}
