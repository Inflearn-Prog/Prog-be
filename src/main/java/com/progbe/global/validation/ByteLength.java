package com.progbe.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 문자열의 UTF-8 인코딩 바이트 수 상한을 검증한다.
 *
 * <p>DB 컬럼 용량은 문자 수가 아니라 바이트로 정해지므로(MySQL TEXT = 65,535 bytes),
 * 저장 가능 여부를 판단하려면 바이트로 세야 한다. 한글은 3바이트, 이모지는 4바이트라
 * {@code @Size} 로는 컬럼 초과를 막을 수 없다.
 *
 * <p>사용자에게 보이는 길이 제한은 {@link PlainTextLength} 가 담당하며,
 * 이 검증은 사용자가 정상 사용 중에는 걸리지 않아야 하는 <b>시스템 안전망</b>이다.
 */
@Documented
@Constraint(validatedBy = ByteLengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteLength {

    String message() default "내용이 저장 가능한 크기를 초과했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** UTF-8 기준 최대 바이트 수. */
    int max();
}
