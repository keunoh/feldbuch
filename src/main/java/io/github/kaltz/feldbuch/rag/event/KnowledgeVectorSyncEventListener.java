package io.github.kaltz.feldbuch.rag.event;

import io.github.kaltz.feldbuch.rag.service.KnowledgeVectorSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorSyncEventListener {

    private final KnowledgeVectorSyncService knowledgeVectorSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KnowledgeVectorSyncEvent event) {

        knowledgeVectorSyncService.sync(event.knowledgeNoteId());
    }
}