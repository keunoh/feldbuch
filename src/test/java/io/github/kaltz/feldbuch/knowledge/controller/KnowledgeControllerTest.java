package io.github.kaltz.feldbuch.knowledge.controller;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthentication;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KnowledgeControllerTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeRepository knowledgeRepository;

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
}