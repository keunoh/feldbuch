package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.event.ConversationRespondedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationStreamCompletionService {

    private final ConversationMessageCommandService messageCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void complete(
            Long userId,
            Long conversationId,
            String userMessage,
            String assistantContent
    ) {
        messageCommandService.createAssistantMessage(
                userId,
                conversationId,
                assistantContent
        );

        eventPublisher.publishEvent(
                new ConversationRespondedEvent(
                        userId,
                        conversationId,
                        userMessage
                )
        );
    }
}
