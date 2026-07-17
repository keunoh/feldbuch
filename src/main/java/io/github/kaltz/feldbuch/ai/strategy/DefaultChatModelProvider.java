package io.github.kaltz.feldbuch.ai.strategy;

import io.github.kaltz.feldbuch.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultChatModelProvider implements ChatModelProvider {

    private final OpenAiProperties properties;

    @Override
    public String getModel() {
        return properties.getModel();
    }
}
