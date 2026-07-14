package io.github.kaltz.feldbuch.conversation.repository;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
