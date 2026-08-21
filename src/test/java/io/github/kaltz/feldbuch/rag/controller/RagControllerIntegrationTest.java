package io.github.kaltz.feldbuch.rag.controller;

import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.rag.dto.RagAnswerRequest;
import io.github.kaltz.feldbuch.rag.model.RagAnswerResult;
import io.github.kaltz.feldbuch.rag.model.RagSource;
import io.github.kaltz.feldbuch.rag.service.RagAnswerService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RagControllerIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    private RagAnswerService ragAnswerService;

    @Test
    @DisplayName("RAG 답변과 검색 출처를 반환한다.")
    void answerWithSources() throws Exception {

        // given
        TestAuthentication authentication = authHelper.createAuthentication();

        String question = "Spring 트랜잭션은 어떻게 사용해?";

        String answer = "Spring에서는 @Transactional을 사용합니다.";

        double score = 0.5281;

        RagSource source = new RagSource(10L, 20L, 30L, score);

        when(
                ragAnswerService.answer(
                        authentication.user().getId(),
                        question)
        ).thenReturn(
                new RagAnswerResult(
                        new ChatResponse(answer),
                        List.of(source)
                )
        );

        String request = """
                    {
                        "question": "%s"
                    }
                """.formatted(question);

        // when & then
        mockMvc.perform(
                        post("/api/rag/answer")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + authentication.accessToken()
                                )
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value(answer))
                .andExpect(jsonPath("$.data.sources.length()").value(1))
                .andExpect(jsonPath("$.data.sources[0].knowledgeNoteId").value(10L))
                .andExpect(jsonPath("$.data.sources[0].knowledgeId").value(20L))
                .andExpect(jsonPath("$.data.sources[0].conversationId").value(30L))
                .andExpect(
                        jsonPath("$.data.sources[0].score")
                                .doesNotExist()
                );
    }

    @Test
    @DisplayName("관련 Knowledge가 없으면 빈 출처 목록을 반환한다.")
    void answerWithoutSources() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        Long userId =
                authentication.user().getId();

        String question =
                "오늘 점심 뭐 먹을까?";

        String answer =
                "샌드위치는 어떠세요?";

        when(
                ragAnswerService.answer(
                        userId,
                        question
                )
        )
                .thenReturn(
                        new RagAnswerResult(
                                new ChatResponse(answer),
                                List.of()
                        )
                );

        RagAnswerRequest request =
                new RagAnswerRequest(
                        question
                );

        // when & then
        mockMvc.perform(
                        post("/api/rag/answer")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer "
                                                + authentication.accessToken()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.data.answer")
                                .value(answer)
                )
                .andExpect(
                        jsonPath("$.data.sources")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.data.sources")
                                .isEmpty()
                );
    }
}