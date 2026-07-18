package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
//@Transactional
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ConversationRepository conversationRepository;

    @Autowired
    protected ConversationMessageRepository conversationMessageRepository;

    protected TestAuthHelper authHelper;
    protected TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        authHelper = new TestAuthHelper(
                mockMvc,
                objectMapper
        );

        testDataFactory = new TestDataFactory(
                mockMvc,
                objectMapper,
                userRepository,
                conversationRepository,
                conversationMessageRepository
        );
    }
}
