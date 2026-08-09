package io.github.kaltz.feldbuch.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /*
     * Common
     */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 내부 오류입니다."),

    /*
     * User
     */
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-001", "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-002", "사용자를 찾을 수 없습니다."),

    /*
     * Authentication / Authorization
     */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-002", "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH-003", "접근 권한이 없습니다."),

    /*
     * Token
     */
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN-001", "Access Token을 확인할 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-002", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN-003", "Refresh Token을 확인할 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-004", "유효하지 않은 Refresh Token입니다."),

    /*
     * Conversation
     */
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONV-001", "대화를 찾을 수 없습니다."),

    /*
     * Knowledge Note
     */
    KNOWLEDGE_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "KNOTE-001", "Knowledge Note를 찾을 수 없습니다."),

    /*
     * AI
     */
    AI_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "AI-001", "AI 작업을 찾을 수 없습니다."),
    OPENAI_UNAUTHORIZED(HttpStatus.BAD_GATEWAY, "AI-002", "OpenAI API 인증에 실패했습니다."),
    OPENAI_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "AI-003", "OpenAI 요청 한도를 초과했습니다."),
    OPENAI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI-004", "OpenAI 응답 시간이 초과되었습니다."),
    OPENAI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AI-005", "OpenAI 서버 오류가 발생했습니다."),

    /*
     * OAuth2
     */
    INVALID_GOOGLE_ACCOUNT(HttpStatus.UNAUTHORIZED, "OAUTH2-001", "유효하지 않은 Google 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}