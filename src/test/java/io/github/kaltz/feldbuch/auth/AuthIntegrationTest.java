package io.github.kaltz.feldbuch.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.user.dto.SignupRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signup() throws Exception {

        SignupRequest request =
                new SignupRequest(
                        "test@test.com",
                        "12345678",
                        "kaltz"
                );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그인 성공")
    void login() throws Exception {

        SignupRequest signup = new SignupRequest(
                "login@test.com",
                "12345678",
                "kaltz"
        );

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest(
                "login@test.com",
                "12345678");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

    }

    @Test
    @DisplayName("JWT 인증 성공")
    void jwtAuthentication() throws Exception {

        // given - 회원가입
        SignupRequest signup = new SignupRequest(
                "jwt@test.com",
                "12345678",
                "kaltz"
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // given - 로그인
        LoginRequest login = new LoginRequest(
                "jwt@test.com",
                "12345678"
        );

        MvcResult loginResult =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andReturn();

        // JWT 추출
        String response =
                loginResult.getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String accessToken =
                json.get("data")
                        .get("accessToken")
                        .asText();

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jwt@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("kaltz"));
    }
}
