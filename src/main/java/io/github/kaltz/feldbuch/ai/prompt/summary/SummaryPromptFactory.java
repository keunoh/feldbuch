package io.github.kaltz.feldbuch.ai.prompt.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryPromptFactory {

    private final SummaryPromptV1 prompt;

    public SummaryPrompt get() {
        return prompt;
    }
}
