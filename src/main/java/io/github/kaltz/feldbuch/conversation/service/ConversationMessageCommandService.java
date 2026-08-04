package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationMessageCommandService {

    private static final String ACTIVITY_LOG =
            "[CONVERSATION_ACTIVITY]";

    private final ConversationReader conversationReader;
    private final ConversationMessageRepository repository;
    private final Clock clock;

    public Long createUserMessage(
            Long userId,
            Long conversationId,
            String content
    ) {
        return createMessage(
                userId,
                conversationId,
                ConversationRole.USER,
                content
        );
    }

    public Long createAssistantMessage(
            Long userId,
            Long conversationId,
            String content
    ) {
        return createMessage(
                userId,
                conversationId,
                ConversationRole.ASSISTANT,
                content
        );
    }

    private Long createMessage(
            Long userId,
            Long conversationId,
            ConversationRole role,
            String content
    ) {
        // TODO:
        // 동시 채팅 요청 시 sequence 충돌 가능성 검토
        Conversation conversation =
                conversationReader.get(
                        userId,
                        conversationId
                );

        int sequence =
                repository.nextSequence(
                        conversationId
                );

        ConversationMessage message =
                ConversationMessage.create(
                        conversation,
                        sequence,
                        role,
                        content
                );

        repository.save(message);

        /*
         * 메시지 저장 시 대화를 활성 상태로 전환하고
         * 마지막 메시지 활동 시각을 갱신한다.
         *
         * 완료된 대화였다면 Conversation 내부에서
         * 다음 증분 지식 추출 상태도 준비한다.
         */
        LocalDateTime messageAt =
                LocalDateTime.now(clock);

        conversation.recordMessageActivity(
                messageAt
        );

        log.debug(
                "{} conversationId={} userId={} role={} sequence={} messageAt={}",
                ACTIVITY_LOG,
                conversationId,
                userId,
                role,
                sequence,
                messageAt
        );

        return message.getId();
    }
}
