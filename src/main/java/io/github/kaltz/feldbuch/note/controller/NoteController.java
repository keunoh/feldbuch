package io.github.kaltz.feldbuch.note.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.response.NoteListResponse;
import io.github.kaltz.feldbuch.note.dto.response.NoteResponse;
import io.github.kaltz.feldbuch.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ApiResponse<NoteResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateNoteRequest request
    ) {

        return ApiResponse.success(
                noteService.create(user.getUserId(), request)
        );
    }

    @GetMapping
    public ApiResponse<List<NoteListResponse>> findAll(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ApiResponse.success(
                noteService.findAll(user.getUserId())
        );
    }

    @GetMapping("/{noteId}")
    public ApiResponse<NoteResponse> findById(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long noteId
    ) {

        return ApiResponse.success(
                noteService.findById(
                        user.getUserId(),
                        noteId
                )
        );
    }

    @PatchMapping("/{noteId}")
    public ApiResponse<NoteResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request
    ) {

        return ApiResponse.success(
                noteService.update(
                        user.getUserId(),
                        noteId,
                        request
                )
        );
    }

    @DeleteMapping("/{noteId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long noteId
    ) {

        noteService.delete(
                user.getUserId(),
                noteId
        );

        return ApiResponse.success(null);
    }
}
