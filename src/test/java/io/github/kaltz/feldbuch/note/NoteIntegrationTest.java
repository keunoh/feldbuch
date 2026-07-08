package io.github.kaltz.feldbuch.note;

import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("노트 생성 성공")
    void createNote() throws Exception {

        String token = authHelper.createAccessToken();

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

    @DisplayName("노트 검색")
    @Test
    void searchNotes() throws Exception {

        String token = authHelper.createAccessToken();

        testDataFactory.createNote(
                token,
                "Spring Security",
                "Spring Security 내용",
                NoteCategory.STUDY
        );

        testDataFactory.createNote(
                token,
                "Docker",
                "Docker 내용",
                NoteCategory.ENVIRONMENT
        );

        testDataFactory.createNote(
                token,
                "JWT Provider",
                "JWT Provider 내용",
                NoteCategory.STUDY
        );

        mockMvc.perform(get("/api/notes")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    @DisplayName("노트 고정")
    @Test
    void pinNote() throws Exception {
        String token = authHelper.createAccessToken();

        Long noteId = testDataFactory.createNote(
                token,
                "Spring",
                "내용",
                NoteCategory.STUDY
        );

        mockMvc.perform(patch("/api/notes/{id}/pin", noteId)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "pinned": true
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
