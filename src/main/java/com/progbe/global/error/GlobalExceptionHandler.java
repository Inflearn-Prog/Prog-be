package com.progbe.global.error;

import com.progbe.global.common.ApiResponse;
import com.progbe.global.error.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Custom Exception을 상속받는 모든 사용자 지정 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(
                        String.valueOf(errorCode.getStatus().value()),
                        errorCode.name(),
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ApiResponse<Void> response = ApiResponse.fail(
                String.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus().value()),
                ErrorCode.INVALID_INPUT_VALUE.name(),
                message != null ? message : ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );

        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        if (e.getRequiredType() != null && e.getRequiredType().equals(LocalDate.class)) {
            ApiResponse<Void> response = ApiResponse.fail(
                    String.valueOf(ErrorCode.INVALID_DATE_FORMAT.getStatus().value()),
                    ErrorCode.INVALID_DATE_FORMAT.name(),
                    ErrorCode.INVALID_DATE_FORMAT.getMessage()
            );
            return new ResponseEntity<>(response, ErrorCode.INVALID_DATE_FORMAT.getStatus());
        }

        if (e.getRequiredType() != null && e.getRequiredType().isEnum()) {
            String typeName = e.getRequiredType().getSimpleName();
            ApiResponse<Void> response = ApiResponse.fail(
                    "400",
                    "INVALID_PARAMETER",
                    String.format("잘못된 %s 값입니다. 입력값을 확인해주세요.", typeName)
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ApiResponse<Void> response = ApiResponse.fail(
                String.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus().value()),
                ErrorCode.INVALID_INPUT_VALUE.name(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ApiResponse<Void> response = ApiResponse.fail(
                String.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value()),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
