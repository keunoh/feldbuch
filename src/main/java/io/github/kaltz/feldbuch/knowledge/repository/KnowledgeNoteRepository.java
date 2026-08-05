package io.github.kaltz.feldbuch.knowledge.repository;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeNoteRepository extends JpaRepository<KnowledgeNote, Long> {

    List<KnowledgeNote>
    findAllByUserIdAndKnowledgeIdOrderByCreatedAtAsc(
            Long userId,
            Long knowledgeId
    );

    Optional<KnowledgeNote>
    findByIdAndUserId(
            Long noteId,
            Long userId
    );

    /**
     * 같은 Conversation의 통합 노트를 조회한다.
     */
    Optional<KnowledgeNote> findFirstByUserIdAndConversationIdAndType(
            Long userId,
            Long conversationId,
            KnowledgeNoteType type
    );

    /**
     * 특정 Knowledge 아래의 Incremental 노트 목록 조회
     */
    List<KnowledgeNote>
    findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
            Long userId,
            Long knowledgeId,
            KnowledgeNoteType type
    );
}
