package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.ai.context.ChatContextBuilder;
import io.github.kaltz.feldbuch.ai.dto.ChatRequest;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationChatService {

    private final ConversationMessageCommandService messageCommandService;
    private final ChatContextBuilder chatContextBuilder;
    private final ChatService chatService;

    // 읽기만 해도
    // USER 저장 > AI 호출 > ASSISTANT 저장 > 응답
    public ChatResponse chat(Long userId, Long conversationId, ChatRequest request) {

        // 1. 사용자 메시지 저장
        messageCommandService.createUserMessage(userId, conversationId, request.message());

        ChatCommand command =
                chatContextBuilder.build(
                        userId,
                        conversationId
                );

        // 4. AI 호출
        ChatResponse response = chatService.chat(command);

        // 5. AI 응답 저장
        messageCommandService.createAssistantMessage(userId, conversationId, response.content());

        return response;
    }
}
