package com.progbe.global.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MeasureExecutionTime {

    long warnThresholdMs() default 3000; //ms 단위 임계값. 초과 시 WARN 레벨로 출력되도록. (google 3-second-rule 기준)
}
