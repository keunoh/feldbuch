package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.knowledge.context.ConversationKnowledgeContextBuilder;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeExtractionService {

    private final UserReader userReader;

    private final ConversationReader conversationReader;

    private final ConversationKnowledgeContextBuilder contextBuilder;

    private final AiKnowledgeSummaryService aiKnowledgeSummaryService;

    private final KnowledgeNoteCommandService knowledgeNoteCommandService;

    public KnowledgeNote extract(Long userId, Long conversationId) {

        User user =
                userReader.get(userId);

        Conversation conversation =
                conversationReader.get(
                        userId,
                        conversationId
                );

        String conversationContext =
                contextBuilder.build(
                        userId,
                        conversationId
                );

        AiKnowledgeSummaryResponse response =
                aiKnowledgeSummaryService.summarize(
                        conversationContext
                );

        return knowledgeNoteCommandService
                .saveAiSummary(
                        user,
                        conversation,
                        response
                );
    }
}
