package io.github.kaltz.feldbuch.conversation.scheduler;

import io.github.kaltz.feldbuch.conversation.service.ConversationCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationCompletionScheduler {

    private static final String COMPLETION_LOG =
            "[CONVERSATION_COMPLETION_SCHEDULER]";

    private final ConversationCompletionService
            completionService;

    @Value(
            "${conversation.auto-completion.inactivity-timeout:30m}"
    )
    private Duration inactivityTimeout;

    @Scheduled(
            fixedDelayString =
                    "${conversation.auto-completion.fixed-delay:60000}"
    )
    public void run() {
        try {
            int completedCount =
                    completionService
                            .completeInactiveConversations(
                                    inactivityTimeout
                            );

            if (completedCount > 0) {
                log.info(
                        "{} Completed inactive conversations. count={}",
                        COMPLETION_LOG,
                        completedCount
                );
            } else {
                log.debug(
                        "{} No inactive conversations.",
                        COMPLETION_LOG
                );
            }
        } catch (Exception exception) {
            log.error(
                    "{} Failed to complete inactive conversations.",
                    COMPLETION_LOG,
                    exception
            );
        }
    }
}
