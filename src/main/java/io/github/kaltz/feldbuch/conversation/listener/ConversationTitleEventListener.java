package io.github.kaltz.feldbuch.conversation.listener;

import io.github.kaltz.feldbuch.ai.model.TitleCommand;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.event.ConversationRespondedEvent;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationTitleEventListener {

    private final ConversationReader conversationReader;
    private final ChatService chatService;

    @Async
    @Transactional
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void generateTitle(
            ConversationRespondedEvent event
    ) {

        Conversation conversation =
                conversationReader.get(
                        event.userId(),
                        event.conversationId()
                );

        if (!conversation.hasDefaultTitle()) {
            return;
        }

        try {
            TitleResponse response =
                    chatService.generateTitle(
                            TitleCommand.from(
                                    event.userMessage()
                            )
                    );

            conversation.changeTitle(
                    response.title()
            );

            log.info(
                    "Conversation title generated. conversationId={} title={}",
                    conversation.getId(),
                    response.title()
            );
        } catch (Exception exception) {
            log.warn(
                    "Failed to generate conversation title. conversationId={}",
                    event.conversationId(),
                    exception
            );
        }
    }
}
