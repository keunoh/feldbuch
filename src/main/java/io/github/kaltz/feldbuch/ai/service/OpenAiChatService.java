package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.client.OpenAiClient;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiRequestMapper;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiResponseMapper;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiChatService implements ChatService {

    private final OpenAiClient openAiClient;
    private final OpenAiRequestMapper requestMapper;
    private final OpenAiResponseMapper responseMapper;

    @Override
    public ChatResponse chat(ChatCommand command) {

        ChatCompletionRequest request = requestMapper.toRequest(command);

        ChatCompletionResponse response = openAiClient.chat(request);

        return responseMapper.toResponse(response);
    }
}
