package io.github.kaltz.feldbuch.knowledge.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteDetailResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteSummaryResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeTreeResponse;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeQueryService queryService;

    @GetMapping("/tree")
    public ApiResponse<List<KnowledgeTreeResponse>> findTree(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ApiResponse.success(
                queryService.findTree(
                        user.getUserId()
                )
        );
    }

    @GetMapping("/notes/{noteId}")
    public ApiResponse<KnowledgeNoteDetailResponse> findNote(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long noteId
    ) {
        return ApiResponse.success(
                queryService.findNote(
                        user.getUserId(),
                        noteId
                )
        );
    }

    @GetMapping("/{knowledgeId}/notes")
    public ApiResponse<List<KnowledgeNoteSummaryResponse>> findNotes(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long knowledgeId
    ) {

        return ApiResponse.success(
                queryService.findNotes(
                        user.getUserId(),
                        knowledgeId
                )
        );
    }
}
