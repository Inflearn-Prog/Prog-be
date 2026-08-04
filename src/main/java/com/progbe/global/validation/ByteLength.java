package com.progbe.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 문자열의 UTF-8 바이트 수 상한을 검증한다.
 *
 * <p>DB 컬럼 용량은 문자가 아니라 바이트 기준이므로 {@code @Size} 로는 초과를 막을 수 없다.
 */
@Documented
@Constraint(validatedBy = ByteLengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteLength {

    String message() default "내용이 저장 가능한 크기를 초과했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max();
}
