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

    private final KnowledgePathResolver knowledgePathResolver;

    private final KnowledgeNoteRepository knowledgeNoteRepository;

    @Transactional
    public KnowledgeNote saveAiSummary(
            User user,
            Conversation conversation,
            AiKnowledgeSummaryResponse response
    ) {

        Knowledge knowledge =
                knowledgePathResolver.resolve(
                        user,
                        response.rootCategory(),
                        response.knowledgePath()
                );

        KnowledgeNote note =
                KnowledgeNote.create(
                        user,
                        conversation,
                        knowledge,
                        response.title(),
                        response.description(),
                        response.summary(),
                        response.keywords()
                );

        return knowledgeNoteRepository.save(note);
    }

    @Transactional
    public KnowledgeNote updateAiSummary(
            User user,
            KnowledgeNote note,
            AiKnowledgeMergeResponse response
    ) {
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

        if (
                !note.getKnowledge()
                        .getId()
                        .equals(knowledge.getId())
        ) {
            note.moveTo(knowledge);
        }

        return note;
    }
}
