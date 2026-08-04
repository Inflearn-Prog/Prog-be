package com.progbe.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class ByteLengthValidator implements ConstraintValidator<ByteLength, String> {

    private int max;

    @Override
    public void initialize(ByteLength constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return utf8Length(value) <= max;
    }

    public static int utf8Length(String value) {
        if (value == null) {
            return 0;
        }
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
