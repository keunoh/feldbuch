package io.github.kaltz.feldbuch.ai.mapper;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.strategy.ChatModelProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiRequestMapper {

    private final ChatModelProvider modelProvider;

    public ChatCompletionRequest toRequest(ChatCommand command) {

        List<Message> messages = command.messages()
                .stream()
                .map(this::toMessage)
                .toList();

        return new ChatCompletionRequest(
                modelProvider.getModel(),
                messages
        );
    }

    private Message toMessage(ChatMessage message) {

        return new Message(
                message.role().name().toLowerCase(),
                message.content()
        );
    }
}
