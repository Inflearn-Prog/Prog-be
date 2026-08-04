package com.progbe.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PlainTextLengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PlainTextLength {

    String message() default "내용이 허용된 길이를 초과했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max();
}
