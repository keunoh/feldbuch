package io.github.kaltz.feldbuch.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),

    // User
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-001", "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-002", "사용자를 찾을 수 없습니다."),

    // Auth
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH-001", "비밀번호가 일치하지 않습니다."),

    // JWT
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER-001", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ATUH-002", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-003", "로그인이 필요합니다."),

    // Note
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTE-001", "노트를 찾을 수 없습니다."),

    // AI
    AI_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "AI-001", "AI 작업을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
