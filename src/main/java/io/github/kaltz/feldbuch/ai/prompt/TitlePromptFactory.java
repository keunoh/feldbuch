package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import io.github.kaltz.feldbuch.ai.model.TitleCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TitlePromptFactory {

    public List<ChatMessage> create(TitleCommand command) {

        return List.of(
                new ChatMessage(
                        ChatRole.SYSTEM,
                        """
                                당신은 대화 제목 생성가이다.
                                
                                규칙
                                - 한국어
                                - 15자 이하
                                - 제목만 반환
                                """
                ),
                new ChatMessage(
                        ChatRole.USER,
                        command.message()
                )
        );
    }
}
