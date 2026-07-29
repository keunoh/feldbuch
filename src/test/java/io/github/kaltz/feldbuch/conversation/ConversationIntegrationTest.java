package io.github.kaltz.feldbuch.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.kaltz.feldbuch.conversation.dto.request.UpdateConversationRequest;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.service.ConversationCommandService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    @DisplayName("Conversation 삭제 시 연결된 Message도 함께 삭제된다")
    void deleteConversation() throws Exception {
        // 1. 인증된 사용자가 자신의 대화를 삭제할 수 있다.
        // 2. 외래 키로 연결된 메시지가 먼저 삭제된다.
        // 3. 메시지 삭제 후 대화도 정상적으로 삭제된다.

        String token = authHelper.createAccessToken();

        String request = """
                    {
                        "title": "삭제할 대화"
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

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow();

        ConversationMessage message = ConversationMessage.create(
                conversation,
                1,
                ConversationRole.USER,
                "삭제 여부를 확인할 메시지"
        );

        conversationMessageRepository.save(message);

        assertThat(conversationRepository.existsById(conversationId))
                .isTrue();

        assertThat(
                conversationMessageRepository
                        .findAllByConversationIdOrderBySequenceAsc(conversationId)
        ).hasSize(1);

        mockMvc.perform(delete("/api/conversations/{id}", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(conversationRepository.existsById(conversationId))
                .isFalse();

        assertThat(
                conversationMessageRepository
                        .findAllByConversationIdOrderBySequenceAsc(conversationId)
        ).isEmpty();

    }

    @Test
    @DisplayName("다른 사용자의 Conversation은 삭제할 수 없다")
    void cannotDeleteOtherUsersConversation() throws Exception {

        String ownToken = authHelper.createAccessToken();
        String otherUserToken = authHelper.createAccessToken();

        String request = """
                {
                    "title": "소유자만 삭제할 수 있는 대화"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        Long conversationId = json.get("data").asLong();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow();

        ConversationMessage message = ConversationMessage.create(
                conversation,
                1,
                ConversationRole.USER,
                "삭제되면 안 되는 메시지"
        );

        conversationMessageRepository.save(message);

        mockMvc.perform(delete("/api/conversations/{id}", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("CONV-001"))
                .andExpect(jsonPath("$.error.message")
                        .value("대화를 찾을 수 없습니다."));

        assertThat(conversationRepository.existsById(conversationId))
                .isTrue();

        assertThat(
                conversationMessageRepository
                        .findAllByConversationIdOrderBySequenceAsc(conversationId)
        ).hasSize(1);
    }

    @Test
    @DisplayName("대화 제목을 수정한다")
    void updateConversationTitle() throws Exception {

        // given
        String token = authHelper.createAccessToken();

        String request = """
                {
                    "title": "새 대화"
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

        UpdateConversationRequest updateRequest
                = new UpdateConversationRequest("Spring Batch 정리");

        // when
        mockMvc.perform(patch("/api/conversations/{conversationId}", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow();

        assertThat(conversation.getTitle())
                .isEqualTo("Spring Batch 정리");

    }

    @Test
    @DisplayName("다른 사용자의 대화 제목은 수정할 수 없다")
    void cannotUpdateOtherUsersConversationTitle() throws Exception {

        // given
        String ownToken = authHelper.createAccessToken();
        String otherUserToken = authHelper.createAccessToken();

        String request = """
                {
                    "title": "소유자의 대화"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        // 소유자의 대화 아이디
        Long conversationId = json.get("data").asLong();

        UpdateConversationRequest updateRequest =
                new UpdateConversationRequest("무단 수정");

        // when
        mockMvc.perform(patch("/api/conversations/{conversationId}", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CONV-001"));

        // then
        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow();

        assertThat(conversation.getTitle())
                .isEqualTo("소유자의 대화");
    }
}
