package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.ai.context.ChatContextBuilder;
import io.github.kaltz.feldbuch.ai.dto.ChatRequest;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.TitleCommand;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConversationChatService {

    private final ConversationMessageCommandService messageCommandService;
    private final ChatContextBuilder chatContextBuilder;
    private final ChatService chatService;
    private final ConversationReader conversationReader;

    // 읽기만 해도
    // USER 저장 > AI 호출 > ASSISTANT 저장 > 응답
    public ChatResponse chat(Long userId, Long conversationId, ChatRequest request) {

        // 조회가 한 번 더 발생하더라도 일관성이 더 큰 가치다.
        Conversation conversation = conversationReader.get(userId, conversationId);

        String message = request.message();

        // 1. 사용자 메시지 저장
        messageCommandService.createUserMessage(userId, conversationId, message);

        ChatCommand command = chatContextBuilder.build(userId, conversationId);

        // 4. AI 호출
        ChatResponse response = chatService.chat(command);

        // 5. AI 응답 저장
        messageCommandService.createAssistantMessage(userId, conversationId, response.content());

        generateConversationTitle(conversation, message);

        return response;
    }

    private void generateConversationTitle(Conversation conversation, String message) {

        if (!conversation.hasDefaultTitle()) {
            return;
        }

        try {
            TitleResponse response = chatService.generateTitle(TitleCommand.from(message));

            conversation.changeTitle(response.title());
        } catch (Exception e) {
            log.warn("Failed to generate conversation title. conversationId={}", conversation.getId(), e);
        }
    }
}
