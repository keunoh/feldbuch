package io.github.kaltz.feldbuch.note.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.common.response.PageResponse;
import io.github.kaltz.feldbuch.note.dto.request.CreateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdateNoteRequest;
import io.github.kaltz.feldbuch.note.dto.request.UpdatePinRequest;
import io.github.kaltz.feldbuch.note.dto.response.NoteListResponse;
import io.github.kaltz.feldbuch.note.dto.response.NoteResponse;
import io.github.kaltz.feldbuch.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<PageResponse<NoteListResponse>> search(

            @AuthenticationPrincipal
            CustomUserDetails user,

            @RequestParam(required = false)
            String keyword,

            @PageableDefault(size = 20)
            Pageable pageable

    ) {

        return ApiResponse.success(

                PageResponse.from(
                        noteService.search(
                                user.getUserId(),
                                keyword,
                                pageable
                        )
                )

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

    @PatchMapping("/{noteId}/pin")
    public ApiResponse<Void> changePinned(
            @AuthenticationPrincipal
            CustomUserDetails user,

            @PathVariable
            Long noteId,

            @RequestBody
            UpdatePinRequest request
    ) {

        noteService.changePinned(
                user.getUserId(),
                noteId,
                request
        );

        return ApiResponse.success(null);
    }
}
