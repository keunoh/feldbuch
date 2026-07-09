package io.github.kaltz.feldbuch.ai.job.controller;

import io.github.kaltz.feldbuch.ai.job.dto.AiJobResponse;
import io.github.kaltz.feldbuch.ai.job.service.AiJobQueryService;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/jobs")
@RequiredArgsConstructor
public class AiJobController {

    private final AiJobQueryService queryService;

    @GetMapping("/{jobId}")
    public ApiResponse<AiJobResponse> findById(@PathVariable Long jobId) {
        return ApiResponse.success(queryService.findById(jobId));
    }
}
