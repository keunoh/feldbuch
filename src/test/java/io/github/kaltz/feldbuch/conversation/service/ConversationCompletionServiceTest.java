package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationCompletionServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private Conversation firstConversation;

    @Mock
    private Conversation secondConversation;

    private ConversationCompletionService completionService;

    @BeforeEach
    void setUp() {
        completionService =
                new ConversationCompletionService(
                        conversationRepository,
                        clock
                );
    }

    private final Clock clock =
            Clock.fixed(
                    Instant.parse(
                            "2026-08-04T03:00:00Z"
                    ),
                    ZoneId.of(
                            "Asia/Seoul"
                    )
            );

    @Test
    void 비활성_대화를_완료한다() {
        // given
        Duration inactivityTimeout =
                Duration.ofMinutes(30);

        LocalDateTime expectedCutoff =
                LocalDateTime.of(
                        2026,
                        8,
                        4,
                        11,
                        30
                );

        when(
                conversationRepository
                        .findInactiveActiveConversations(
                                expectedCutoff
                        )
        ).thenReturn(
                List.of(
                        firstConversation,
                        secondConversation
                )
        );

        // when
        int result =
                completionService
                        .completeInactiveConversations(
                                inactivityTimeout
                        );

        // then
        assertThat(result)
                .isEqualTo(2);

        verify(firstConversation)
                .complete();

        verify(secondConversation)
                .complete();
    }

    @Test
    void 비활성_대화가_없으면_0을_반환한다() {
        // given
        Duration inactivityTimeout =
                Duration.ofMinutes(30);

        LocalDateTime expectedCutoff =
                LocalDateTime.of(
                        2026,
                        8,
                        4,
                        11,
                        30
                );

        when(
                conversationRepository
                        .findInactiveActiveConversations(
                                expectedCutoff
                        )
        ).thenReturn(
                List.of()
        );

        // when
        int result =
                completionService
                        .completeInactiveConversations(
                                inactivityTimeout
                        );

        // then
        assertThat(result)
                .isZero();
    }

    @Test
    void 비활성_제한_시간이_0이면_실패한다() {
        assertThatThrownBy(() ->
                completionService
                        .completeInactiveConversations(
                                Duration.ZERO
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "대화 비활성 제한 시간은 0보다 커야 합니다."
                );
    }

    @Test
    void 비활성_제한_시간이_음수면_실패한다() {
        assertThatThrownBy(() ->
                completionService
                        .completeInactiveConversations(
                                Duration.ofMinutes(-1)
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }
}