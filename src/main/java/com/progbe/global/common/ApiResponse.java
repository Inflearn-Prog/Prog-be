package com.progbe.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final T data;
    private final ErrorBody error;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, String code, T data, ErrorBody error) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    // =========== 성공 응답 ===========
    public static <T> ApiResponse<T> success(T data, String code) {
        return new ApiResponse<>(
                true,
                code,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                String.valueOf(HttpStatus.OK.value()),
                data,
                null
        );
    }

    // =========== 실패 응답 ===========
    public static <T> ApiResponse<T> fail(String httpStatusCode, String errorName, String message) {
        return new ApiResponse<>(
                false,
                httpStatusCode,
                null,
                new ErrorBody(errorName, message)
        );
    }

    public record ErrorBody(String errorClassName, String message) {
        /* nop */
    }
}