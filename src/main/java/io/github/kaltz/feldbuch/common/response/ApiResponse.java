package io.github.kaltz.feldbuch.common.response;

import io.github.kaltz.feldbuch.common.exception.ErrorDetail;
import lombok.Builder;

@Builder
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error
) {

    public static <T> ApiResponse<T> success(T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static <T> ApiResponse<T> fail(ErrorDetail error) {

        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }
}
