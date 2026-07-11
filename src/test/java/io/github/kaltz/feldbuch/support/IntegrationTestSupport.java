package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                objectMapper
        );
    }
}
