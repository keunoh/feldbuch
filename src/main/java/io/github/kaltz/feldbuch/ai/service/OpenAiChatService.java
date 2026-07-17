package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class OpenAiChatService implements ChatService {

    @Override
    public ChatResponse chat(ChatCommand command) {
        throw new UnsupportedOperationException("구현 예정");
    }
}
