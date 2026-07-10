package io.github.kaltz.feldbuch.ai.prompt.summary;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;

public interface SummaryPrompt {

    String create(SummaryRequest request);
}
