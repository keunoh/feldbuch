package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class TestDataFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    public Long createNote(String token, String title, String content, NoteCategory category) throws Exception {
        CreateNoteRequest request =
                new CreateNoteRequest(title, content, category);

        MvcResult result = mockMvc.perform(post("/api/notes")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return json.get("data")
                .get("id")
                .asLong();
    }

    public User createUser() {
        User user = User.builder()
                .email(UUID.randomUUID() + "@test.com")
                .password("password")
                .nickname("tester")
                .build();

        return userRepository.save(user);
    }

    public Conversation createConversation(User user) {
        Conversation conversation = Conversation.create(user);

        return conversationRepository.save(conversation);
    }

    public Conversation createConversation(User user, String title) {
        Conversation conversation = Conversation.create(user, title);

        return conversationRepository.save(conversation);
    }

    public ConversationMessage createUserMessage(Conversation conversation, String content) {
        ConversationMessage message = ConversationMessage.create(conversation, 1, ConversationRole.USER, content);

        return conversationMessageRepository.save(message);
    }

    public ConversationMessage createAssistantMessage(Conversation conversation, String content) {
        int sequence = conversationMessageRepository.nextSequence(conversation.getId());

        ConversationMessage message = ConversationMessage.create(conversation, sequence, ConversationRole.ASSISTANT, content);

        return conversationMessageRepository.save(message);
    }
}
