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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationKnowledgeContextBuilderTest {

    @Mock
    private ConversationMessageReader messageReader;

    @InjectMocks
    private ConversationKnowledgeContextBuilder contextBuilder;

    private User user;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .role(UserRole.USER)
                .build();

        conversation = Conversation.create(
                user,
                "Spring Batch 학습"
        );
    }

    @Test
    void 대화_메시지를_학습용_문자열로_변환한다() {
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

        ConversationMessage assistantMessage =
                ConversationMessage.create(
                        conversation,
                        2,
                        ConversationRole.ASSISTANT,
                        "Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다."
                );

        when(messageReader.findAll(userId, conversationId))
                .thenReturn(
                        List.of(
                                userMessage,
                                assistantMessage
                        )
                );

        // when
        String result =
                contextBuilder.build(
                        userId,
                        conversationId
                );

        // then
        String expected = """
                USER:
                Spring Batch가 무엇인지 설명해줘.
                
                AI:
                Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다.
                """.trim();

        assertThat(result).isEqualTo(expected);

        verify(messageReader)
                .findAll(userId, conversationId);
    }

    @Test
    void 메시지가_없으면_빈_문자열을_반환한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        when(messageReader.findAll(userId, conversationId))
                .thenReturn(List.of());

        // when
        String result =
                contextBuilder.build(
                        userId,
                        conversationId
                );

        // then
        assertThat(result).isEmpty();

        verify(messageReader)
                .findAll(userId, conversationId);
    }
}