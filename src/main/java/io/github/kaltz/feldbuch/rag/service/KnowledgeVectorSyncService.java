package io.github.kaltz.feldbuch.rag.service;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeVectorSyncService {

    private final KnowledgeNoteRepository knowledgeNoteRepository;

    private final KnowledgeVectorStore knowledgeVectorStore;

    @Transactional(readOnly = true)
    public void sync(Long knowledgeNoteId) {
        KnowledgeNote note =
                knowledgeNoteRepository
                        .findById(knowledgeNoteId)
                        .orElseThrow(
                                () -> new IllegalStateException("KnowledgeNote를 찾을 수 없습니다.")
                        );

        knowledgeVectorStore.save(note);
    }
}