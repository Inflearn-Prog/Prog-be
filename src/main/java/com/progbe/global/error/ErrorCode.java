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

    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "닉네임은 한글, 영문, 숫자만 사용할 수 있으며 최대 12자입니다."),
    NICKNAME_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    NICKNAME_CHANGE_TOO_FREQUENT(HttpStatus.BAD_REQUEST, "닉네임은 24시간에 한 번만 변경할 수 있습니다."),
    INVALID_CAREER_YEAR(HttpStatus.BAD_REQUEST, "유효하지 않은 경력 연차 값입니다."),

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 문의사항입니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답변입니다."),

    ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 답변이 완료된 문의사항입니다."),
    NOT_QUESTION_WRITER(HttpStatus.FORBIDDEN, "해당 문의사항의 작성자가 아닙니다."),
    NOT_ADMIN(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.FORBIDDEN, "자신의 권한은 변경할 수 없습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    NOT_COMMENT_WRITER(HttpStatus.FORBIDDEN, "댓글 작성자가 아닙니다."),
    REPLY_DEPTH_LIMIT(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다. (1-depth 제한)"),
    INVALID_COMMENT_PROMPT(HttpStatus.BAD_REQUEST, "해당 프롬프트의 댓글이 아닙니다."),

    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 하며, YYYY-MM-DD 형식을 준수해야 합니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식을 사용해주세요."),
    FUTURE_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "미래 날짜는 조회할 수 없습니다."),
    DATE_RANGE_EXCEEDED(HttpStatus.BAD_REQUEST, "조회 가능한 최대 기간은 30일입니다."),

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리 정보가 포함되어 있습니다."),
    PROMPTS_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 프롬프트 ID 중 존재하는 게시글이 없습니다."),

    CANNOT_REPORT_SELF(HttpStatus.BAD_REQUEST, "자신의 컨텐츠를 신고할 수 없습니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 신고된 항목입니다."),
    TEXT_TOO_LONG(HttpStatus.BAD_REQUEST, "200자를 초과할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고 내역입니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리되었거나 존재하지 않는 신고 내역입니다."),
    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 페이징 또는 정렬 요청입니다."),
    DATABASE_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "서버 응답이 지연되고 있습니다. 관리자에게 문의하세요."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),

    // 5XX
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류로 작업에 실패했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 트래픽 폭주로 인해 요청을 처리할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 오류입니다."),
    CONSTRUCTION_NOT_ALLOWED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 오류입니다."),
    STATISTICS_NOT_READY(HttpStatus.INTERNAL_SERVER_ERROR, "통계 데이터 집계가 완료되지 않았습니다. 잠시 후 다시 시도해주세요."),
    DATA_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 변환 중 오류가 발생했습니다."),
    DATA_INTEGRITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청한 유저 정보 중 일부를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
