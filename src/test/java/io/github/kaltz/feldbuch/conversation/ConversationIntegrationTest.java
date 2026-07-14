package io.github.kaltz.feldbuch.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.kaltz.feldbuch.conversation.service.ConversationCommandService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConversationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ConversationCommandService conversationCommandService;

    @Test
    @DisplayName("Conversation 생성")
    void createConversation() throws Exception {

        String token = authHelper.createAccessToken();

        String request = """
                {
                    "title": "Spring Batch 공부"
                }
                """;

        mockMvc.perform(post("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("Conversation 조회")
    void findConversation() throws Exception {

        String token = authHelper.createAccessToken();

        String request = """
                {
                    "title": "Spring Batch 공부"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        Long conversationId = json.get("data").asLong();

        mockMvc.perform(get("/api/conversations/{id}", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(conversationId));

    }
}
