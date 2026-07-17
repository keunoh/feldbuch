package io.github.kaltz.feldbuch.ai.mapper;

import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMessageMapper {

    public List<ChatMessage> toChatMessages(List<ConversationMessage> messages) {

        return messages.stream()
                .map(this::toChatMessage)
                .toList();
    }

    public ChatMessage toChatMessage(ConversationMessage message) {

        return new ChatMessage(
                convertRole(message.getRole()), message.getContent()
        );
    }

    private ChatRole convertRole(ConversationRole role) {
        return switch (role) {
            case USER -> ChatRole.USER;
            case ASSISTANT -> ChatRole.ASSISTANT;
        };
    }

}
