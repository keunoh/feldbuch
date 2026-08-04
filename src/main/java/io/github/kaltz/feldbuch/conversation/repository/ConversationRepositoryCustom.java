package io.github.kaltz.feldbuch.conversation.repository;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;

import java.util.List;

public interface ConversationRepositoryCustom {

    List<Conversation> findKnowledgeExtractionTargets();

    boolean existsKnowledgeExtractionTarget();

}
