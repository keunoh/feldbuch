package io.github.kaltz.feldbuch.ai.context;

import io.github.kaltz.feldbuch.ai.mapper.ChatMessageMapper;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.reader.ConversationMessageReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatContextBuilder {

    private final ConversationMessageReader messageReader;
    private final ChatMessageMapper mapper;

    public ChatCommand build(Long userId, Long conversationId) {

        List<ConversationMessage> messages =
                messageReader.findAll(userId, conversationId);

        List<ChatMessage> chatMessages =
                mapper.toChatMessages(messages);

        return ChatCommand.from(chatMessages);
    }
}
