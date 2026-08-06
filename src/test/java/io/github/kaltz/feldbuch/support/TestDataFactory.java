package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@RequiredArgsConstructor
public class TestDataFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;

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
}
