package io.github.kaltz.feldbuch.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.entity.NoteCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class TestDataFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public Long createNote(
            String token,
            String title,
            String content,
            NoteCategory category
    ) throws Exception {
        CreateNoteRequest request =
                new CreateNoteRequest(
                        title,
                        content,
                        category
                );

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
}
