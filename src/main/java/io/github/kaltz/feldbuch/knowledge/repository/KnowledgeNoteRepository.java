package io.github.kaltz.feldbuch.knowledge.repository;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeNoteRepository extends JpaRepository<KnowledgeNote, Long> {

    List<KnowledgeNote>
    findAllByUserIdAndKnowledgeIdOrderByCreatedAtAsc(
            Long userId,
            Long knowledgeId
    );

    List<KnowledgeNote>
    findAllByUserIdAndConversationIdOrderByCreatedAtAsc(
            Long userId,
            Long conversationId
    );

    List<KnowledgeNote>
    findAllByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<KnowledgeNote>
    findByIdAndUserId(
            Long noteId,
            Long userId
    );

    Optional<KnowledgeNote>
    findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
            Long userId,
            Long conversationId
    );

    boolean existsByUserIdAndConversationId(
            Long userId,
            Long conversationId
    );
}
