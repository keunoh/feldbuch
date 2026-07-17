package io.github.kaltz.feldbuch.ai.mapper;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class OpenAiResponseMapper {

    public ChatResponse toResponse(ChatCompletionResponse response) {

        String content = response.choices()
                .getFirst()
                .message()
                .content();

        return new ChatResponse(content);
    }
}
