package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCompletionService {

    private static final String COMPLETION_LOG =
            "[CONVERSATION_COMPLETION]";

    private final ConversationRepository
            conversationRepository;

    private final Clock clock;

    @Transactional
    public int completeInactiveConversations(
            Duration inactivityTimeout
    ) {
        validateInactivityTimeout(
                inactivityTimeout
        );

        LocalDateTime cutoff =
                LocalDateTime.now(clock)
                        .minus(
                                inactivityTimeout
                        );

        List<Conversation> targets =
                conversationRepository
                        .findInactiveActiveConversations(
                                cutoff
                        );

        for (Conversation conversation : targets) {
            conversation.complete();

            log.debug(
                    "{} Conversation completed. conversationId={} lastMessageAt={} cutoff={}",
                    COMPLETION_LOG,
                    conversation.getId(),
                    conversation.getLastMessageAt(),
                    cutoff
            );
        }

        return targets.size();
    }

    private void validateInactivityTimeout(
            Duration inactivityTimeout
    ) {
        if (
                inactivityTimeout == null
                        || inactivityTimeout.isZero()
                        || inactivityTimeout.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "대화 비활성 제한 시간은 0보다 커야 합니다."
            );
        }
    }
}
