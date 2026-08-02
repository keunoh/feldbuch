package io.github.kaltz.feldbuch.knowledge.repository;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeNoteRepository extends JpaRepository<KnowledgeNote, Long> {

    /**
     * 특정 Knowledge 안의 노트 목록 조회
     */
    List<KnowledgeNote> findAllByUserIdAndKnowledgeIdOrderByCreatedAtAsc(Long userId, Long knowledgeId);

    /**
     * 한 대화에서 생성된 모든 학습 노트 조회
     */
    List<KnowledgeNote> findAllByUserIdAndConversationIdOrderByCreatedAtAsc(Long userId, Long conversationId);

    /**
     * 사용자의 최근 학습 노트 조회
     */
    List<KnowledgeNote> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<KnowledgeNote> findByIdAndUserId(Long noteId, Long userId);

    boolean existsByUserIdAndConversationId(Long userId, Long conversationId);
}
