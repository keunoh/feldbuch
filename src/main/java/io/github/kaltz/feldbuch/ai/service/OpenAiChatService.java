package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.client.OpenAiClient;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiRequestMapper;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiResponseMapper;
import io.github.kaltz.feldbuch.ai.model.*;
import io.github.kaltz.feldbuch.ai.prompt.TitlePromptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiChatService implements ChatService {

    private final OpenAiClient openAiClient;
    private final OpenAiRequestMapper requestMapper;
    private final OpenAiResponseMapper responseMapper;
    private final TitlePromptFactory titlePromptFactory;

    @Override
    public ChatResponse chat(ChatCommand command) {

        ChatCompletionResponse response = request(command.messages());

        return responseMapper.toChatResponse(response);
    }

    @Override
    public TitleResponse generateTitle(TitleCommand command) {

        // Prompt 생성
        // Request Mapper
        // OpenAI 호출
        // Response Mapper

        ChatCompletionResponse response =
                request(titlePromptFactory.create(command));

        return responseMapper.toTitleResponse(response);
    }

    private ChatCompletionResponse request(List<ChatMessage> messages) {

        ChatCompletionRequest request =
                requestMapper.toRequest(
                        ChatCommand.from(messages)
                );

        return openAiClient.chat(request);
    }
}
