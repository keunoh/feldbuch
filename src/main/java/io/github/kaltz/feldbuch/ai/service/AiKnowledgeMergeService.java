package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

public interface AiKnowledgeMergeService {

    AiKnowledgeMergeResponse merge(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    );
}
