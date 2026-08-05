package io.github.kaltz.feldbuch.knowledge.controller;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthentication;
import io.github.kaltz.feldbuch.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KnowledgeControllerTest extends IntegrationTestSupport {

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private KnowledgeNoteRepository knowledgeNoteRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    @DisplayName("Knowledge 트리 조회 성공")
    void findKnowledgeTree() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        String token =
                authentication.accessToken();

        User user =
                authentication.user();

        Knowledge development =
                knowledgeRepository.save(
                        Knowledge.createRoot(
                                user,
                                "개발"
                        )
                );

        Knowledge spring =
                knowledgeRepository.save(
                        Knowledge.createChild(
                                user,
                                development,
                                "Spring"
                        )
                );

        knowledgeRepository.save(
                Knowledge.createChild(
                        user,
                        spring,
                        "Spring WebFlux"
                )
        );

        knowledgeRepository.save(
                Knowledge.createRoot(
                        user,
                        "AI"
                )
        );

        // when & then
        mockMvc.perform(
                        get("/api/knowledge/tree")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data[0].name")
                                .value("개발")
                )
                .andExpect(
                        jsonPath(
                                "$.data[0].children[0].name"
                        )
                                .value("Spring")
                )
                .andExpect(
                        jsonPath("$.data[0].children[0].children[0].name")
                                .value("Spring WebFlux")
                )
                .andExpect(
                        jsonPath("$.data[1].name")
                                .value("AI")
                );
    }

    @Test
    @DisplayName("Knowledge가 없으면 빈 트리를 반환한다")
    void findEmptyKnowledgeTree() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        // when & then
        mockMvc.perform(
                        get("/api/knowledge/tree")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + authentication.accessToken()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("KnowledgeNote 상세 조회")
    void findKnowledgeNote() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        String token =
                authentication.accessToken();

        User user =
                authentication.user();

        Conversation conversation =
                conversationRepository.save(
                        Conversation.create(user, "Spring Batch")
                );

        Knowledge knowledge =
                knowledgeRepository.save(
                        Knowledge.createRoot(
                                user,
                                "개발"
                        )
                );

        KnowledgeNote note =
                knowledgeNoteRepository.save(
                        KnowledgeNote.createConsolidated(
                                user,
                                conversation,
                                knowledge,
                                "Spring Batch 기본 구조",
                                "Job과 Step을 중심으로 실행 구조를 설명",
                                "Spring Batch는 Job과 Step으로 작업을 구성합니다.",
                                List.of(
                                        "Spring Batch",
                                        "Job",
                                        "Step"
                                )
                        )
                );

        // when & then
        mockMvc.perform(
                        get("/api/knowledge/notes/{noteId}",
                                note.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.id")
                        .value(note.getId()))
                .andExpect(jsonPath("$.data.title")
                        .value("Spring Batch 기본 구조"))
                .andExpect(jsonPath("$.data.description")
                        .value("Job과 Step을 중심으로 실행 구조를 설명"))
                .andExpect(jsonPath("$.data.summary")
                        .value("Spring Batch는 Job과 Step으로 작업을 구성합니다."))
                .andExpect(jsonPath("$.data.keywords.length()")
                        .value(3))
                .andExpect(jsonPath("$.data.keywords[0]")
                        .value("Spring Batch"))
                .andExpect(jsonPath("$.data.keywords[1]")
                        .value("Job"))
                .andExpect(jsonPath("$.data.keywords[2]")
                        .value("Step"));
    }

    @Test
    @DisplayName("통합 노트를 조회한다.")
    void findConsolidatedNote() throws Exception {

        TestAuthentication authentication =
                authHelper.createAuthentication();

        User user =
                authentication.user();

        String token =
                authentication.accessToken();

        Conversation conversation =
                conversationRepository.save(
                        Conversation.create(
                                user,
                                "Spring Batch"
                        )
                );

        Knowledge knowledge =
                knowledgeRepository.save(
                        Knowledge.createRoot(
                                user,
                                "웹 개발"
                        )
                );

        KnowledgeNote incremental =
                knowledgeNoteRepository.save(
                        KnowledgeNote.createIncremental(
                                user,
                                conversation,
                                knowledge,
                                "Tasklet",
                                "Tasklet 설명",
                                "Tasklet 내용",
                                List.of(
                                        "Tasklet"
                                )
                        )
                );

        KnowledgeNote consolidated =
                knowledgeNoteRepository.save(
                        KnowledgeNote.createConsolidated(
                                user,
                                conversation,
                                knowledge,
                                "Spring Batch",
                                "통합 노트",
                                "최종 정리",
                                List.of(
                                        "Spring Batch",
                                        "Tasklet"
                                )
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/knowledge/conversations/{conversationId}/consolidated-note",
                                conversation.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.id")
                        .value(consolidated.getId()))
                .andExpect(jsonPath("$.data.title")
                        .value("Spring Batch"))
                .andExpect(jsonPath("$.data.summary")
                        .value("최종 정리"));
    }
}