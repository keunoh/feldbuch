package io.github.kaltz.feldbuch.rag.prompt;

import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagPromptFactoryTest {

    private final RagPromptFactory ragPromptFactory = new RagPromptFactory();

    @Test
    void 검색된_지식과_질문으로_RAG_프롬프트를_생성한다() {

        // given
        String context =
                """
                        [지식 1]
                        제목: Spring 트랜잭션
                        내용:
                        @Transactional을 사용하여 트랜잭션을 관리합니다.
                        """;

        String question = "Spring에서 트랜잭션은 어떻게 사용하지?";

        // when
        List<ChatMessage> messages = ragPromptFactory.create(question, context);

        // then
        assertThat(messages).hasSize(2);

        ChatMessage systemMessage = messages.get(0);
        ChatMessage userMessage = messages.get(1);

        assertThat(systemMessage.role()).isEqualTo(ChatRole.SYSTEM);
        assertThat(userMessage.role()).isEqualTo(ChatRole.USER);

        assertThat(userMessage.content())
                .contains("Spring 트랜잭션")
                .contains("@Transactional")
                .contains(question);
    }

}