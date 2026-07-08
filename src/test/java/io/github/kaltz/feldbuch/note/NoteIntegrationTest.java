package io.github.kaltz.feldbuch.note;

import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.support.TestAuthHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Test
    @DisplayName("노트 생성 성공")
    void createNote() throws Exception {

        String token = testAuthHelper.createAccessToken();

        CreateNoteRequest request = new CreateNoteRequest(
                "Spring Security",
                "JWT 공부",
                NoteCategory.STUDY
        );

        mockMvc.perform(post("/api/notes")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Spring Security"))
                .andExpect(jsonPath("$.data.category").value("STUDY"));
    }

}
