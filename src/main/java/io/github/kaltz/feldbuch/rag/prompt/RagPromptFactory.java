package io.github.kaltz.feldbuch.rag.prompt;

import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RagPromptFactory {

    public List<ChatMessage> create(String question, String context) {

        ChatMessage systemMessage =
                new ChatMessage(
                        ChatRole.SYSTEM,
                        """
                                당신은 사용자의 개인 지식 노트를 참고하여 답변하는 AI입니다.
                                
                                아래 제공되는 지식은 사용자가 이전에 정리한 내용입니다.
                                
                                답변 규칙:
                                - 제공된 지식을 우선적으로 참고합니다.
                                - 지식에 없는 내용을 임의로 사실처럼 만들지 않습니다.
                                - 제공된 지식만으로 답변하기 어려우면 그 사실을 명확히 설명합니다.
                                - 답변은 자연스럽고 이해하기 쉽게 작성합니다.
                                """
                );

        ChatMessage userMessage =
                new ChatMessage(
                        ChatRole.USER,
                        """
                                [사용자의 지식]
                                
                                %s
                                
                                [질문]
                                
                                %s
                                """
                                .formatted(
                                        context,
                                        question
                                )
                );

        return List.of(systemMessage, userMessage);
    }
}
