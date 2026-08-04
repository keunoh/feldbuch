package io.github.kaltz.feldbuch.knowledge.context;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.reader.ConversationMessageReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationAiContextBuilder {

    private final ConversationMessageReader messageReader;

    public ConversationAiContext build(
            Conversation conversation
    ) {
        validateConversation(conversation);

        Long userId =
                conversation.getUser()
                        .getId();

        Long conversationId =
                conversation.getId();

        List<ConversationMessage> messages =
                messageReader.findAfter(
                        userId,
                        conversationId,
                        conversation.getLastExtractedMessageId()
                );

        String content =
                createContent(messages);

        return new ConversationAiContext(
                messages,
                content
        );
    }

    private String createContent(
            List<ConversationMessage> messages
    ) {
        StringBuilder builder =
                new StringBuilder();

        for (ConversationMessage message : messages) {
            builder.append(role(message))
                    .append(System.lineSeparator())
                    .append(message.getContent())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return builder
                .toString()
                .trim();
    }

    private String role(
            ConversationMessage message
    ) {
        return switch (message.getRole()) {
            case USER -> "USER:";
            case ASSISTANT -> "AI:";
        };
    }

    private void validateConversation(
            Conversation conversation
    ) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "대화는 필수입니다."
            );
        }

        if (conversation.getId() == null) {
            throw new IllegalArgumentException(
                    "저장되지 않은 대화로 AI 컨텍스트를 만들 수 없습니다."
            );
        }

        if (
                conversation.getUser() == null
                        || conversation.getUser().getId() == null
        ) {
            throw new IllegalArgumentException(
                    "대화 사용자 정보가 없습니다."
            );
        }
    }
}
