package io.github.kaltz.feldbuch.ai;

import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("노트 요약")
    void summarizeNote() throws Exception {

        String token = authHelper.createAccessToken();

        Long noteId = testDataFactory.createNote(
                token,
                "Spring Security",
                "Spring Security는 인증과 인가를 담당하는 프레임워크이다...",
                NoteCategory.STUDY
        );

        mockMvc.perform(
                        post("/api/ai/notes/{id}/summary",
                                noteId)

                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
