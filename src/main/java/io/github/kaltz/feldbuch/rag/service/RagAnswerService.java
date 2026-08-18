package io.github.kaltz.feldbuch.rag.service;

import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.rag.context.KnowledgeContextBuilder;
import io.github.kaltz.feldbuch.rag.prompt.RagPromptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagAnswerService {

    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeContextBuilder knowledgeContextBuilder;
    private final RagPromptFactory ragPromptFactory;
    private final ChatService chatService;

    public ChatResponse answer(Long userId, String question) {

        List<Document> documents = knowledgeSearchService.search(userId, question);

        String context = knowledgeContextBuilder.build(documents);

        List<ChatMessage> messages = ragPromptFactory.create(question, context);

        ChatCommand command = ChatCommand.from(messages);

        return chatService.chat(command);
    }
}
