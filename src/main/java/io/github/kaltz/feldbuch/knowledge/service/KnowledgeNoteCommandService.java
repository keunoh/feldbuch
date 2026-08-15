package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.rag.event.KnowledgeVectorSyncEvent;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeNoteCommandService {

    private final KnowledgeCategoryResolver
            knowledgeCategoryResolver;

    private final KnowledgeNoteRepository
            knowledgeNoteRepository;

    private final ApplicationEventPublisher
            eventPublisher;

    /**
     * 새롭게 추출된 대화 범위를
     * 개별 학습 노트로 저장한다.
     * <p>
     * 배치가 실행될 때마다 새로운
     * INCREMENTAL 노트가 생성된다.
     */
    @Transactional
    public KnowledgeNote saveIncremental(
            User user,
            Conversation conversation,
            AiKnowledgeSummaryResponse response
    ) {
        validateUser(user);
        validateConversation(conversation);
        validateSummaryResponse(response);

        Knowledge knowledge =
                resolveKnowledge(
                        user,
                        response.category()
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
     * 같은 대화의 내용을 누적 관리하는
     * 통합 노트를 최초 생성한다.
     */
    @Transactional
    public KnowledgeNote saveConsolidated(
            User user,
            Conversation conversation,
            AiKnowledgeSummaryResponse response
    ) {
        validateUser(user);
        validateConversation(conversation);
        validateSummaryResponse(response);

        Knowledge knowledge =
                resolveKnowledge(
                        user,
                        response.category()
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

        KnowledgeNote saved =
                knowledgeNoteRepository.save(
                        note
                );

        eventPublisher.publishEvent(
                new KnowledgeVectorSyncEvent(
                        saved.getId()
                )
        );

        return saved;
    }

    /**
     * 기존 통합 노트를
     * AI 병합 결과로 갱신한다.
     * <p>
     * 병합 결과의 카테고리가 달라졌다면
     * 통합 노트를 해당 카테고리 폴더로 이동한다.
     */
    @Transactional
    public KnowledgeNote updateConsolidated(
            User user,
            KnowledgeNote note,
            AiKnowledgeMergeResponse response
    ) {
        validateUser(user);
        validateConsolidatedNote(note);
        validateMergeResponse(response);

        Knowledge resolvedKnowledge =
                resolveKnowledge(
                        user,
                        response.category()
                );

        note.updateContent(
                response.title(),
                response.description(),
                response.summary(),
                response.keywords()
        );

        if (!sameKnowledge(note.getKnowledge(), resolvedKnowledge)) {

            note.moveTo(resolvedKnowledge);
        }

        eventPublisher.publishEvent(new KnowledgeVectorSyncEvent(note.getId()));

        return note;
    }

    private Knowledge resolveKnowledge(User user, KnowledgeCategory category) {

        return knowledgeCategoryResolver.resolve(user, category);
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
        ) {
            return false;
        }

        Long currentId =
                current.getId();

        Long resolvedId =
                resolved.getId();

        if (
                currentId == null
                        || resolvedId == null
        ) {
            return false;
        }

        return currentId.equals(
                resolvedId
        );
    }

    private void validateUser(
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "저장되지 않은 사용자는 KnowledgeNote를 저장할 수 없습니다."
            );
        }
    }

    private void validateConversation(
            Conversation conversation
    ) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "원본 대화는 필수입니다."
            );
        }
    }

    private void validateSummaryResponse(
            AiKnowledgeSummaryResponse response
    ) {
        if (response == null) {
            throw new IllegalArgumentException(
                    "AI 지식 요약 응답은 필수입니다."
            );
        }

        validateCategory(
                response.category()
        );
    }

    private void validateMergeResponse(
            AiKnowledgeMergeResponse response
    ) {
        if (response == null) {
            throw new IllegalArgumentException(
                    "AI 지식 병합 응답은 필수입니다."
            );
        }

        validateCategory(
                response.category()
        );
    }

    private void validateCategory(
            KnowledgeCategory category
    ) {
        if (category == null) {
            throw new IllegalArgumentException(
                    "Knowledge 카테고리는 필수입니다."
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