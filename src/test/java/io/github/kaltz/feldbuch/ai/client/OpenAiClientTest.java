package io.github.kaltz.feldbuch.ai.client;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OpenAiClientTest {

    @Autowired
    OpenAiClient openAiClient;

    @Autowired
    OpenAiProperties properties;

    @Test
    void properties() {

        System.out.println("api-key=" + properties.getApiKey());
        System.out.println("base-url=" + properties.getBaseUrl());
        System.out.println("model=" + properties.getModel());


        assertThat(properties.getApiKey())
                .isNotBlank()
                .isNotEqualTo("${OPENAI_API_KEY}");
        assertThat(properties.getBaseUrl())
                .isEqualTo("https://api.openai.com/v1");
        assertThat(properties.getModel())
                .isEqualTo("gpt-4.1-nano");
    }

    @Test
    void chat() {

        ChatCompletionRequest request =
                new ChatCompletionRequest(
                        properties.getModel(),
                        List.of(
                                new Message(
                                        "user",
                                        "테스트코드 잘 짜는 법 쌈박하게 알려줘"
                                )
                        )
                );

        ChatCompletionResponse response =
                openAiClient.chat(request);

        System.out.println(
                response.choices()
                        .getFirst()
                        .message()
                        .content()
        );
    }

}
