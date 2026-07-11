package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.user.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class TestAuthHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public String createAccessToken() throws Exception {

        String email = UUID.randomUUID() + "@test.com";

        SignupRequest signup = new SignupRequest(
                email,
                "12345678",
                "kaltz"
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest(
                email,
                "12345678"
        );

        MvcResult result =
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode json =
                objectMapper.readTree(
                        result.getResponse().getContentAsString()
                );

        return json.get("data")
                .get("accessToken")
                .asText();
    }
}
