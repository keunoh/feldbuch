package io.github.kaltz.feldbuch.conversation;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.service.ConversationCommandService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConversationControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ConversationCommandService conversationCommandService;

    @Test
    @DisplayName("대화 목록을 조회한다")
    void findAll() throws Exception {
        // given
        TestAuthentication authentication = authHelper.createAuthentication();

        conversationRepository.save(
                Conversation.create(authentication.user(), "Spring")
        );

        conversationRepository.save(
                Conversation.create(authentication.user(), "Docker")
        );

        conversationRepository.save(
                Conversation.create(authentication.user(), "JWT")
        );

        // when & then
        mockMvc.perform(get("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + authentication.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @DisplayName("자신의 대화만 조회한다.")
    void findAll_onlyMine() throws Exception {
        // given
        TestAuthentication authentication = authHelper.createAuthentication();

        conversationRepository.save(
                Conversation.create(authentication.user(), "Spring")
        );

        conversationRepository.save(
                Conversation.create(authentication.user(), "Docker")
        );

        TestAuthentication other = authHelper.createAuthentication();

        conversationRepository.save(
                Conversation.create(other.user(), "Secret")
        );

        // when & then
        mockMvc.perform(get("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + authentication.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Docker"))
                .andExpect(jsonPath("$.data[1].title").value("Spring"));
    }
}
