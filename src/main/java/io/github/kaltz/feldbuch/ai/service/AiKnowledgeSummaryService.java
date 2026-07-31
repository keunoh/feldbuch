package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;

public interface AiKnowledgeSummaryService {

    AiKnowledgeSummaryResponse summarize(String conversation);
}
