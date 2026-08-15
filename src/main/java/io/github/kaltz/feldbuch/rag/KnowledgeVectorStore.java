package io.github.kaltz.feldbuch.rag;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorStore {

    private final VectorStore vectorStore;

    public void save(KnowledgeNote note) {
        validateNote(note);

        String documentId = documentId(note.getId());

        Document document =
                new Document(
                        documentId,
                        buildContent(note),
                        Map.of(
                                "type", "knowledge",
                                "knowledgeNoteId", note.getId(),
                                "userId", note.getUser().getId(),
                                "conversationId",
                                note.getConversation().getId(),
                                "knowledgeId",
                                note.getKnowledge().getId(),
                                "noteType",
                                note.getType().name()
                        )
                );

        vectorStore.delete(List.of(documentId));

        vectorStore.add(List.of(document));
    }

    private String documentId(Long knowledgeNoteId) {
        return UUID.nameUUIDFromBytes(
                ("knowledge-note-" + knowledgeNoteId)
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        ).toString();
    }

    private String buildContent(
            KnowledgeNote note
    ) {
        return """
                제목: %s
                설명: %s
                내용:
                %s
                키워드: %s
                """.formatted(
                note.getTitle(),
                note.getDescription(),
                note.getSummary(),
                String.join(
                        ", ",
                        note.getKeywords()
                )
        );
    }

    private void validateNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "KnowledgeNote는 필수입니다."
            );
        }

        if (note.getId() == null) {
            throw new IllegalArgumentException(
                    "저장된 KnowledgeNote만 벡터화할 수 있습니다."
            );
        }

        if (!note.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트만 벡터화할 수 있습니다."
            );
        }
    }
}