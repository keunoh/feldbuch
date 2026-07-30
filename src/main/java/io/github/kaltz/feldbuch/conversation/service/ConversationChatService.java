package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.ai.context.ChatContextBuilder;
import io.github.kaltz.feldbuch.ai.dto.ChatRequest;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.TitleCommand;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.conversation.dto.response.StreamResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationChatService {

    private static final String STREAM_LOG = "[CHAT_STREAM]";

    private final ConversationMessageCommandService messageCommandService;
    private final ChatContextBuilder chatContextBuilder;
    private final ChatService chatService;
    private final ConversationReader conversationReader;

    // 읽기만 해도
    // USER 저장 > AI 호출 > ASSISTANT 저장 > 응답
    @Transactional
    public ChatResponse chat(Long userId, Long conversationId, ChatRequest request) {

        // 조회가 한 번 더 발생하더라도 일관성이 더 큰 가치다.
        Conversation conversation =
                conversationReader.get(userId, conversationId);

        String message = request.message();

        // 1. 사용자 메시지 저장
        messageCommandService.createUserMessage(
                userId,
                conversationId,
                message
        );

        ChatCommand command =
                chatContextBuilder.build(userId, conversationId);

        // 4. AI 호출
        ChatResponse response =
                chatService.chat(command);

        // 5. AI 응답 저장
        messageCommandService.createAssistantMessage(
                userId,
                conversationId,
                response.content()
        );

        generateConversationTitle(conversation, message);

        return response;
    }

    public Flux<StreamResponse> stream(Long userId, Long conversationId, ChatRequest request) {

        // 사용자가 접근할 수 있는 대화인지 먼저 검증한다.
        conversationReader.get(userId, conversationId);

        String userMessage = request.message();

        // 짧은 트랜잭션으로 사용자 메시지를 저장한다.
        messageCommandService.createUserMessage(
                userId,
                conversationId,
                userMessage
        );

        // 방금 저장한 사용자 메시지까지 포함해 AI 문맥을 생성한다.
        ChatCommand command =
                chatContextBuilder.build(userId, conversationId);

        StringBuilder assistantContent =
                new StringBuilder();

        log.info(
                "{} Started. userId={} conversationId={}",
                STREAM_LOG,
                userId,
                conversationId
        );

        return chatService.stream(command)
                .doOnNext(assistantContent::append)
                .map(StreamResponse::token)
                .concatWith(
                        Mono.fromCallable(() -> {
                            String content =
                                    assistantContent.toString();

                            messageCommandService.createAssistantMessage(
                                    userId,
                                    conversationId,
                                    content
                            );

                            log.info(
                                    "{} Completed. userId={} conversationId={} contentLength={}",
                                    STREAM_LOG,
                                    userId,
                                    conversationId,
                                    content.length()
                            );

                            return StreamResponse.complete();
                        })
                )
                .doOnCancel(() -> {
                    log.info(
                            "{} Cancelled. userId={} conversationId={} receivedLength={}",
                            STREAM_LOG,
                            userId,
                            conversationId,
                            assistantContent.length()
                    );
                })
                .doOnError(exception ->
                        log.warn(
                                "{} Failed. userId={} conversationId={} receivedLength={} exception={}",
                                STREAM_LOG,
                                userId,
                                conversationId,
                                assistantContent.length(),
                                exception.getClass().getSimpleName()
                        )
                );
    }

    private void generateConversationTitle(Conversation conversation, String message) {

        if (!conversation.hasDefaultTitle()) {
            return;
        }

        try {
            TitleResponse response =
                    chatService.generateTitle(
                            TitleCommand.from(message)
                    );

            conversation.changeTitle(response.title());
        } catch (Exception e) {
            log.warn(
                    "Failed to generate conversation title. conversationId={}"
                    , conversation.getId()
                    , e
            );
        }
    }
}
