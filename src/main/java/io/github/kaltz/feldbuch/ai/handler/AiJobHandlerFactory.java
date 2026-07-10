package io.github.kaltz.feldbuch.ai.handler;

import io.github.kaltz.feldbuch.ai.job.entity.AiJobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiJobHandlerFactory {

    private final List<AiJobHandler> handlers;

    public AiJobHandler get(AiJobType type) {
        return handlers.stream()
                .filter(handler -> handler.support() == type)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("지원하지 않는 AI Job :" + type));
    }

}
