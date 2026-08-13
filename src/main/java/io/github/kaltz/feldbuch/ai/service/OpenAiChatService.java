package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.client.OpenAiClient;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiRequestMapper;
import io.github.kaltz.feldbuch.ai.mapper.OpenAiResponseMapper;
import io.github.kaltz.feldbuch.ai.model.*;
import io.github.kaltz.feldbuch.ai.prompt.TitlePromptFactory;
import io.github.kaltz.feldbuch.common.time.NanoTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiChatService implements ChatService {

    private static final String OPENAI_LOG = "[OPENAI]";

    private static final String CHAT_OPERATION = "CHAT";
    private static final String TITLE_OPERATION = "TITLE";

    private static final long SLOW_TTFT_THRESHOLD_MS = 10_000L;

    private final OpenAiClient openAiClient;
    private final OpenAiRequestMapper requestMapper;
    private final OpenAiResponseMapper responseMapper;
    private final TitlePromptFactory titlePromptFactory;
    private final NanoTimeProvider nanoTimeProvider;

    @Override
    public ChatResponse chat(ChatCommand command) {

        ChatCompletionResponse response =
                request(CHAT_OPERATION, command.messages());

        return responseMapper.toChatResponse(response);
    }

    @Override
    public Flux<String> stream(ChatCommand command) {
        /** Reactive 생명주기
         * Flux
         *     .doOnNext(...)
         *     .doOnComplete(...)
         *     .doOnError(...)
         * */

        ChatCompletionRequest request =
                requestMapper.toRequest(command);

        long startTime = nanoTimeProvider.nanoTime();

        AtomicBoolean firstTokenReceived =
                new AtomicBoolean(false);

        log.info(
                "{} Stream started. messageCount={}",
                OPENAI_LOG,
                command.messages().size()
        );

        return openAiClient.stream(request)
                .doOnNext(token -> {
                    if (firstTokenReceived.compareAndSet(false, true)) {

                        long ttft =
                                elapseMillis(startTime);

                        log.info(
                                "{} First token received. ttft={}ms",
                                OPENAI_LOG,
                                ttft
                        );

                        if (ttft >= SLOW_TTFT_THRESHOLD_MS) {
                            log.warn(
                                    "[OPENAI_TTFT_SLOW] ttft={}ms threshold={}ms",
                                    ttft,
                                    SLOW_TTFT_THRESHOLD_MS
                            );
                        }
                    }
                })
                .doOnComplete(() ->
                        log.info(
                                "{} Stream completed. elapsed={}ms",
                                OPENAI_LOG,
                                elapseMillis(startTime)
                        )
                )
                .doOnError(exception ->
                        log.warn(
                                "{} Stream failed. elapsed={}ms exception={}",
                                OPENAI_LOG,
                                elapseMillis(startTime),
                                exception.getClass().getSimpleName()
                        )
                );
    }

    @Override
    public TitleResponse generateTitle(TitleCommand command) {

        // Prompt 생성
        // Request Mapper
        // OpenAI 호출
        // Response Mapper

        List<ChatMessage> messages =
                titlePromptFactory.create(command);

        ChatCompletionResponse response =
                request(TITLE_OPERATION, messages);

        return responseMapper.toTitleResponse(response);
    }

    private ChatCompletionResponse request(
            String operation,
            List<ChatMessage> messages
    ) {

        ChatCompletionRequest request =
                requestMapper.toRequest(
                        ChatCommand.from(messages)
                );

        long startTime = nanoTimeProvider.nanoTime();

        log.info(
                "{} Request started. operation={} messageCount={}",
                OPENAI_LOG,
                operation,
                messages.size()
        );

        try {
            ChatCompletionResponse response =
                    openAiClient.chat(request);

            long elapsedMillis =
                    elapseMillis(startTime);

            log.info(
                    "{} Request completed. operation={} elapsed={}ms",
                    OPENAI_LOG,
                    operation,
                    elapsedMillis
            );

            return response;
        } catch (Exception e) {
            long elapsedMillis =
                    elapseMillis(startTime);

            log.warn(
                    "{} Request failed. operation={} elapsed={}ms exception={}",
                    OPENAI_LOG,
                    operation,
                    elapsedMillis,
                    e.getClass().getSimpleName()
            );

            throw e;
        }
    }

    private long elapseMillis(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(
                nanoTimeProvider.nanoTime() - startTime
        );
    }
}
