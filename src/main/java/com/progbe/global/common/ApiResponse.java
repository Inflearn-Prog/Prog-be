package com.progbe.global.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private final String statusCode;
    private final T data;
    private final ErrorBody error;
    private final LocalDateTime timestamp;

    private ApiResponse(String statusCode, T data, ErrorBody error) {
        this.statusCode = statusCode;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data, String code) {
        return new ApiResponse<>(code, data, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", data, null); // Default success code =  200
    }


    public static <T> ApiResponse<T> fail(String httpStatusCode, String errorName, String message) {
        return new ApiResponse<>(httpStatusCode, null, new ErrorBody(errorName, message));
    }

    public record ErrorBody(String code, String message) {
        /* nop */
    }
}