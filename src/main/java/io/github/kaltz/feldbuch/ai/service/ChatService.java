package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;

public interface ChatService {

    ChatResponse chat(ChatCommand command);
}
