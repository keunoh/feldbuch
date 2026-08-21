package io.github.kaltz.feldbuch.rag.context;

import io.github.kaltz.feldbuch.ai.context.ChatContextBuilder;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import io.github.kaltz.feldbuch.rag.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RagChatContextBuilder {

    private final ChatContextBuilder chatContextBuilder;
    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeContextBuilder knowledgeContextBuilder;

    public ChatCommand build(Long userId, Long conversationId, String question) {

        ChatCommand conversationCommand = chatContextBuilder.build(userId, conversationId);

        List<Document> documents = knowledgeSearchService.search(userId, question);

        if (documents.isEmpty()) {

            return conversationCommand;
        }

        String knowledgeContext = knowledgeContextBuilder.build(documents);

        List<ChatMessage> messages = getChatMessages(knowledgeContext);

        messages.addAll(conversationCommand.messages());

        return ChatCommand.from(messages);
    }

    @NonNull
    private static List<ChatMessage> getChatMessages(String knowledgeContext) {
        ChatMessage knowledgeMessage = new ChatMessage(
                ChatRole.SYSTEM,
                """
                        다음 내용은 사용자가 이전에 정리한 개인 지식입니다.
                        
                        사용자의 질문에 답변할 때 관련이 있다면 우선적으로 참고하세요.
                        제공된 지식에 없는 내용을 지식에 있었다고 표현하지 마세요.
                        
                        [사용자의 지식]
                        
                        %s
                        """
                        .formatted(knowledgeContext)
        );

        List<ChatMessage> messages = new ArrayList<>();

        messages.add(knowledgeMessage);

        return messages;
    }
}
