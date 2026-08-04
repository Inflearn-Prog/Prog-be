package com.progbe.global.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ByteLengthValidator - UTF-8 바이트 기준 저장 가능 크기 검증")
class ByteLengthValidatorTest {

    private static final int TEXT_COLUMN_CAPACITY = 65535;
    private static final int MAX = 60000;

    private boolean isValid(String content) {
        ByteLengthValidator validator = new ByteLengthValidator();
        validator.initialize(new ByteLength() {
            @Override public Class<java.lang.annotation.Annotation> annotationType() { return null; }
            @Override public String message() { return ""; }
            @Override public Class<?>[] groups() { return new Class[0]; }
            @Override public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override public int max() { return MAX; }
        });
        return validator.isValid(content, null);
    }

    @Test
    @DisplayName("한글은 3바이트, 이모지는 4바이트로 센다")
    void 문자별_바이트_수를_정확히_센다() {
        assertThat(ByteLengthValidator.utf8Length("a")).isEqualTo(1);
        assertThat(ByteLengthValidator.utf8Length("가")).isEqualTo(3);
        assertThat(ByteLengthValidator.utf8Length("😀")).isEqualTo(4);
    }

    @Test
    @DisplayName("P2-18 회귀: @Size(max=20000) 이 통과시키던 이모지 20,000자는 TEXT 를 초과한다")
    void 이모지_20000자는_컬럼_용량을_초과하므로_거부한다() {
        String content = "😀".repeat(20000);

        assertThat(content.codePointCount(0, content.length())).isEqualTo(20000);
        assertThat(ByteLengthValidator.utf8Length(content)).isEqualTo(80000);
        assertThat(ByteLengthValidator.utf8Length(content)).isGreaterThan(TEXT_COLUMN_CAPACITY);

        assertThat(isValid(content)).isFalse();
    }

    @Test
    @DisplayName("상한을 통과한 값은 항상 TEXT 컬럼에 저장 가능하다")
    void 통과한_값은_컬럼_용량_안에_들어간다() {
        assertThat(MAX).isLessThan(TEXT_COLUMN_CAPACITY);
    }

    @Test
    @DisplayName("평문 5,000자(한글) + 줄바꿈 2,500회는 통과한다 - 정상 사용에서는 걸리지 않아야 한다")
    void 정상적인_본문은_통과한다() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2500; i++) {
            sb.append("<p>").append("가".repeat(2)).append("</p>");
        }
        String content = sb.toString();

        assertThat(PlainTextLengthValidator.stripHtml(content).length()).isEqualTo(5000);
        assertThat(ByteLengthValidator.utf8Length(content)).isLessThan(MAX);
        assertThat(isValid(content)).isTrue();
    }

    @Test
    @DisplayName("본문에 박힌 base64 이미지는 거부한다 - 이미지 차단의 서버측 최종 방어선")
    void base64_이미지가_박힌_본문은_거부한다() {
        String base64 = "A".repeat(68267); // 50KB PNG 의 base64 길이
        String content = "<p>안녕하세요</p><img src=\"data:image/png;base64," + base64 + "\">";

        assertThat(PlainTextLengthValidator.stripHtml(content).length()).isEqualTo(5);
        assertThat(isValid(content)).isFalse();
    }

    @Test
    @DisplayName("경계값 - 정확히 60,000바이트는 통과하고 60,001바이트는 거부한다")
    void 경계값을_정확히_처리한다() {
        assertThat(isValid("a".repeat(MAX))).isTrue();
        assertThat(isValid("a".repeat(MAX + 1))).isFalse();
    }

    @Test
    @DisplayName("null 은 통과시킨다 (@NotBlank 가 담당)")
    void null_은_통과시킨다() {
        assertThat(isValid(null)).isTrue();
        assertThat(ByteLengthValidator.utf8Length(null)).isZero();
    }
}
