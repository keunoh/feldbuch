package io.github.kaltz.feldbuch.ai.mapper;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;
import org.springframework.stereotype.Component;

@Component
public class OpenAiResponseMapper {

    public ChatResponse toChatResponse(ChatCompletionResponse response) {
        return new ChatResponse(extractContent(response));
    }

    public TitleResponse toTitleResponse(ChatCompletionResponse response) {
        return new TitleResponse(extractContent(response));
    }

    private String extractContent(ChatCompletionResponse response) {

        return response.choices()
                .getFirst()
                .message()
                .content()
                .trim();
    }
}
