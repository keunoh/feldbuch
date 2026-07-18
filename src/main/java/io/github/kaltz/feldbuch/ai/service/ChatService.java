package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.TitleCommand;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;

public interface ChatService {

    ChatResponse chat(ChatCommand command);

    TitleResponse generateTitle(TitleCommand command);
}
