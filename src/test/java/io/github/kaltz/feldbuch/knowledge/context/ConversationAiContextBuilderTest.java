package io.github.kaltz.feldbuch.knowledge.context;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.reader.ConversationMessageReader;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationAiContextBuilderTest {

    @Mock
    private ConversationMessageReader messageReader;

    @InjectMocks
    private ConversationAiContextBuilder contextBuilder;

    private User user;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user =
                User.builder()
                        .email("test@test.com")
                        .password("password")
                        .nickname("tester")
                        .role(UserRole.USER)
                        .build();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        ReflectionTestUtils.setField(
                conversation,
                "id",
                1L
        );
    }

    @Test
    void 대화_메시지를_AI_컨텍스트로_변환한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        ConversationMessage userMessage =
                ConversationMessage.create(
                        conversation,
                        1,
                        ConversationRole.USER,
                        "Spring Batch가 무엇인지 설명해줘."
                );

        ReflectionTestUtils.setField(
                userMessage,
                "id",
                10L
        );

        ConversationMessage assistantMessage =
                ConversationMessage.create(
                        conversation,
                        2,
                        ConversationRole.ASSISTANT,
                        "Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다."
                );

        ReflectionTestUtils.setField(
                assistantMessage,
                "id",
                11L
        );

        when(
                messageReader.findAfter(
                        userId,
                        conversationId,
                        null
                )
        ).thenReturn(
                List.of(
                        userMessage,
                        assistantMessage
                )
        );

        // when
        ConversationAiContext result =
                contextBuilder.build(
                        conversation
                );

        // then
        String expectedContent = """
                USER:
                Spring Batch가 무엇인지 설명해줘.
                
                AI:
                Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다.
                """.trim();

        assertThat(result.content())
                .isEqualTo(expectedContent);

        assertThat(result.messages())
                .containsExactly(
                        userMessage,
                        assistantMessage
                );

        assertThat(result.firstMessageId())
                .isEqualTo(10L);

        assertThat(result.lastMessageId())
                .isEqualTo(11L);

        assertThat(result.messageCount())
                .isEqualTo(2);

        assertThat(result.isEmpty())
                .isFalse();

        verify(messageReader)
                .findAfter(
                        userId,
                        conversationId,
                        null
                );
    }

    @Test
    void 메시지가_없으면_빈_AI_컨텍스트를_반환한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        when(
                messageReader.findAfter(
                        userId,
                        conversationId,
                        null
                )
        ).thenReturn(
                List.of()
        );

        // when
        ConversationAiContext result =
                contextBuilder.build(
                        conversation
                );

        // then
        assertThat(result.content())
                .isEmpty();

        assertThat(result.messages())
                .isEmpty();

        assertThat(result.firstMessageId())
                .isNull();

        assertThat(result.lastMessageId())
                .isNull();

        assertThat(result.messageCount())
                .isZero();

        assertThat(result.isEmpty())
                .isTrue();

        verify(messageReader)
                .findAfter(
                        userId,
                        conversationId,
                        null
                );
    }

    @Test
    void 기존_체크포인트_이후의_메시지만_조회한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;
        Long lastExtractedMessageId = 20L;

        ReflectionTestUtils.setField(
                conversation,
                "lastExtractedMessageId",
                lastExtractedMessageId
        );

        ConversationMessage newUserMessage =
                ConversationMessage.create(
                        conversation,
                        3,
                        ConversationRole.USER,
                        "증분 추출은 어떻게 동작해?"
                );

        ReflectionTestUtils.setField(
                newUserMessage,
                "id",
                21L
        );

        ConversationMessage newAssistantMessage =
                ConversationMessage.create(
                        conversation,
                        4,
                        ConversationRole.ASSISTANT,
                        "마지막 체크포인트 이후 메시지만 조회합니다."
                );

        ReflectionTestUtils.setField(
                newAssistantMessage,
                "id",
                22L
        );

        when(
                messageReader.findAfter(
                        userId,
                        conversationId,
                        lastExtractedMessageId
                )
        ).thenReturn(
                List.of(
                        newUserMessage,
                        newAssistantMessage
                )
        );

        // when
        ConversationAiContext result =
                contextBuilder.build(
                        conversation
                );

        // then
        assertThat(result.messageCount())
                .isEqualTo(2);

        assertThat(result.firstMessageId())
                .isEqualTo(21L);

        assertThat(result.lastMessageId())
                .isEqualTo(22L);

        assertThat(result.content())
                .contains(
                        "증분 추출은 어떻게 동작해?",
                        "마지막 체크포인트 이후 메시지만 조회합니다."
                );

        verify(messageReader)
                .findAfter(
                        userId,
                        conversationId,
                        lastExtractedMessageId
                );
    }
}