package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.client.OpenAiClient;
import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.ai.prompt.SummaryPromptTemplate;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class OpenAiSummaryService implements SummaryService {

    private final OpenAiClient openAiClient;

    private final OpenAiProperties properties;

    private final SummaryPromptTemplate promptTemplate;

    @Override
    public String summarize(SummaryRequest request) {

        String prompt = promptTemplate.create(request);

        ChatCompletionRequest chatRequest = new ChatCompletionRequest(
                properties.getModel(),
                List.of(
                        new Message(
                                "user",
                                prompt
                        )
                )
        );

        ChatCompletionResponse response = openAiClient.chat(chatRequest);

        return response
                .choices()
                .getFirst()
                .message()
                .content();
    }
}
