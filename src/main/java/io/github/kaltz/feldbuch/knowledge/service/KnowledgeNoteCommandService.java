package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeNoteCommandService {

    private final KnowledgePathResolver
            knowledgePathResolver;

    private final KnowledgeNoteRepository
            knowledgeNoteRepository;

    /**
     * 새롭게 추출된 대화 범위를 개별 학습 노트로 저장한다.
     * <p>
     * 배치가 실행될 때마다 새로운 INCREMENTAL 노트가 생성된다.
     */
    @Transactional
    public KnowledgeNote saveIncremental(
            User user,
            Conversation conversation,
            AiKnowledgeSummaryResponse response
    ) {
        validateSummaryResponse(response);

        Knowledge knowledge =
                resolveKnowledge(
                        user,
                        response
                );

        KnowledgeNote note =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        knowledge,
                        response.title(),
                        response.description(),
                        response.summary(),
                        response.keywords()
                );

        return knowledgeNoteRepository.save(
                note
        );
    }

    /**
     * 같은 대화의 내용을 누적 관리하는 통합 노트를 최초 생성한다.
     */
    @Transactional
    public KnowledgeNote saveConsolidated(
            User user,
            Conversation conversation,
            AiKnowledgeSummaryResponse response
    ) {
        validateSummaryResponse(response);

        Knowledge knowledge =
                resolveKnowledge(
                        user,
                        response
                );

        KnowledgeNote note =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        knowledge,
                        response.title(),
                        response.description(),
                        response.summary(),
                        response.keywords()
                );

        return knowledgeNoteRepository.save(
                note
        );
    }

    /**
     * 기존 통합 노트를 AI 병합 결과로 갱신한다.
     */
    @Transactional
    public KnowledgeNote updateConsolidated(
            User user,
            KnowledgeNote note,
            AiKnowledgeMergeResponse response
    ) {
        validateMergeResponse(response);
        validateConsolidatedNote(note);

        Knowledge knowledge =
                knowledgePathResolver.resolve(
                        user,
                        response.rootCategory(),
                        response.knowledgePath()
                );

        note.updateContent(
                response.title(),
                response.description(),
                response.summary(),
                response.keywords()
        );

        if (!sameKnowledge(
                note.getKnowledge(),
                knowledge
        )) {
            note.moveTo(
                    knowledge
            );
        }

        return note;
    }

    private Knowledge resolveKnowledge(
            User user,
            AiKnowledgeSummaryResponse response
    ) {
        return knowledgePathResolver.resolve(
                user,
                response.rootCategory(),
                response.knowledgePath()
        );
    }

    private boolean sameKnowledge(
            Knowledge current,
            Knowledge resolved
    ) {
        if (current == resolved) {
            return true;
        }

        if (
                current == null
                        || resolved == null
                        || current.getId() == null
                        || resolved.getId() == null
        ) {
            return false;
        }

        return current.getId()
                .equals(
                        resolved.getId()
                );
    }


    private void validateSummaryResponse(
            AiKnowledgeSummaryResponse response
    ) {
        if (response == null) {
            throw new IllegalArgumentException(
                    "AI 지식 요약 응답은 필수입니다."
            );
        }
    }

    private void validateMergeResponse(
            AiKnowledgeMergeResponse response
    ) {
        if (response == null) {
            throw new IllegalArgumentException(
                    "AI 지식 병합 응답은 필수입니다."
            );
        }
    }

    private void validateConsolidatedNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "갱신할 KnowledgeNote는 필수입니다."
            );
        }

        if (!note.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트만 갱신할 수 있습니다."
            );
        }
    }
}
