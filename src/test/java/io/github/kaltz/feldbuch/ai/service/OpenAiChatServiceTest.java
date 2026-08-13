package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.client.OpenAiClient;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiRequestMapper;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiResponseMapper;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.prompt.TitlePromptFactory;
import io.github.kaltz.feldbuch.common.time.NanoTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiChatServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private OpenAiRequestMapper requestMapper;

    @Mock
    private OpenAiResponseMapper responseMapper;

    @Mock
    private TitlePromptFactory titlePromptFactory;

    @Mock
    private NanoTimeProvider nanoTimeProvider;

    private OpenAiChatService openAiChatService;

    @BeforeEach
    void setUp() {
        openAiChatService =
                new OpenAiChatService(
                        openAiClient,
                        requestMapper,
                        responseMapper,
                        titlePromptFactory,
                        nanoTimeProvider
                );
    }

    @Test
    @DisplayName("OpenAI 스트리밍 응답을 순서대로 전달한다")
    void stream() {

        // given
        ChatCommand command =
                mock(ChatCommand.class);

        ChatCompletionRequest request =
                mock(ChatCompletionRequest.class);

        when(requestMapper.toRequest(command))
                .thenReturn(request);

        when(openAiClient.stream(request))
                .thenReturn(
                        Flux.just(
                                "안녕",
                                "하세요",
                                "!"
                        )
                );

        when(nanoTimeProvider.nanoTime())
                .thenReturn(
                        0L,
                        TimeUnit.MILLISECONDS.toNanos(500),
                        TimeUnit.MILLISECONDS.toNanos(1_000)
                );

        // when
        Flux<String> result =
                openAiChatService.stream(command);

        // then
        StepVerifier.create(result)
                .expectNext("안녕")
                .expectNext("하세요")
                .expectNext("!")
                .verifyComplete();

        verify(requestMapper)
                .toRequest(command);

        verify(openAiClient)
                .stream(request);
    }

    @Test
    @DisplayName("첫 토큰 응답이 10초 이상 걸려도 스트림은 정상적으로 완료된다")
    void streamSlowTtft() {

        // given
        ChatCommand command =
                mock(ChatCommand.class);

        ChatCompletionRequest request =
                mock(ChatCompletionRequest.class);

        when(requestMapper.toRequest(command))
                .thenReturn(request);

        when(openAiClient.stream(request))
                .thenReturn(Flux.just("응답"));

        when(nanoTimeProvider.nanoTime())
                .thenReturn(
                        0L,
                        TimeUnit.MILLISECONDS.toNanos(12_000),
                        TimeUnit.MILLISECONDS.toNanos(12_500)
                );

        // when
        Flux<String> result =
                openAiChatService.stream(command);

        // then
        StepVerifier.create(result)
                .expectNext("응답")
                .verifyComplete();
    }
}