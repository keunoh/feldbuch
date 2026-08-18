package io.github.kaltz.feldbuch.rag.controller;

import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.rag.dto.RagAnswerRequest;
import io.github.kaltz.feldbuch.rag.service.RagAnswerService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RagControllerTest extends IntegrationTestSupport {

    @MockitoBean
    private RagAnswerService ragAnswerService;

    @Test
    @DisplayName("RAG 질문에 답변한다.")
    void answer() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        Long userId =
                authentication.user().getId();

        String question =
                "Spring 트랜잭션은 어떻게 사용해?";

        String answer =
                "Spring에서는 @Transactional을 사용합니다.";

        when(
                ragAnswerService.answer(
                        userId,
                        question
                )
        )
                .thenReturn(
                        new ChatResponse(answer)
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
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.answer")
                                .value(answer)
                );

        verify(
                ragAnswerService
        )
                .answer(
                        userId,
                        question
                );
    }

    @Test
    @DisplayName("질문이 비어 있으면 400을 반환한다.")
    void answerBlankQuestion() throws Exception {

        // given
        TestAuthentication authentication =
                authHelper.createAuthentication();

        RagAnswerRequest request =
                new RagAnswerRequest("");

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
                        status().isBadRequest()
                );
    }
}