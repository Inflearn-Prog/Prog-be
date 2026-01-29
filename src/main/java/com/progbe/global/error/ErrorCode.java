package com.progbe.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@SuppressWarnings("unused")
@Getter
public enum ErrorCode {

    // 4XX
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 기능입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프롬프트입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효하지 않은 소셜 프로바이더입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    SOCIAL_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 연동 해제에 실패했습니다."),

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 문의사항입니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답변입니다."),

    ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 답변이 완료된 문의사항입니다."),
    NOT_QUESTION_WRITER(HttpStatus.FORBIDDEN, "해당 문의사항의 작성자가 아닙니다."),
    NOT_ADMIN(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),

    // 5XX
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류로 작업에 실패했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 트래픽 폭주로 인해 요청을 처리할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 오류입니다."),
    CONSTRUCTION_NOT_ALLOWED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 오류입니다."),
    DATA_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 변환 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
