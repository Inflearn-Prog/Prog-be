package com.progbe.global.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlainTextLengthValidator - 평문 기준 길이 검증")
class PlainTextLengthValidatorTest {

    private static final int MAX = 5000;

    private boolean isValid(String content) {
        PlainTextLengthValidator validator = new PlainTextLengthValidator();
        validator.initialize(new PlainTextLength() {
            @Override public Class<java.lang.annotation.Annotation> annotationType() { return null; }
            @Override public String message() { return ""; }
            @Override public Class<?>[] groups() { return new Class[0]; }
            @Override public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override public int max() { return MAX; }
        });
        return validator.isValid(content, null);
    }

    private String quillHtml(int lines, int charsPerLine) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            sb.append("<p>").append("가".repeat(charsPerLine)).append("</p>");
        }
        return sb.toString();
    }

    @Test
    @DisplayName("P2-18 회귀: 평문 4,900자 + 줄바꿈 50회는 통과해야 한다")
    void 평문은_한도내인데_원문이_초과하는_경우_통과한다() {
        String content = quillHtml(50, 98);

        assertThat(PlainTextLengthValidator.stripHtml(content).length()).isEqualTo(4900);
        assertThat(content.length()).isGreaterThan(MAX);
        assertThat(isValid(content)).isTrue();
    }

    @Test
    @DisplayName("평문이 5,001자면 거부한다")
    void 평문이_한도를_넘으면_거부한다() {
        String content = "<p>" + "가".repeat(5001) + "</p>";

        assertThat(PlainTextLengthValidator.stripHtml(content).length()).isEqualTo(5001);
        assertThat(isValid(content)).isFalse();
    }

    @Test
    @DisplayName("평문이 정확히 5,000자면 통과한다")
    void 경계값_5000자는_통과한다() {
        String content = "<p>" + "가".repeat(5000) + "</p>";

        assertThat(isValid(content)).isTrue();
    }

    @Test
    @DisplayName("null 은 통과시킨다 (@NotBlank 가 담당)")
    void null_은_통과시킨다() {
        assertThat(isValid(null)).isTrue();
    }

    @Test
    @DisplayName("태그를 제거한다")
    void 태그를_제거한다() {
        assertThat(PlainTextLengthValidator.stripHtml("<p><strong>안녕</strong>하세요</p>"))
                .isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("HTML 엔티티를 디코딩한다 - 프론트엔드 stripHtml 과 동일")
    void 엔티티를_디코딩한다() {
        assertThat(PlainTextLengthValidator.stripHtml("a&amp;b")).isEqualTo("a&b");
        assertThat(PlainTextLengthValidator.stripHtml("&lt;tag&gt;")).isEqualTo("<tag>");
        assertThat(PlainTextLengthValidator.stripHtml("&#65;&#66;")).isEqualTo("AB");
        assertThat(PlainTextLengthValidator.stripHtml("&#x41;&#x42;")).isEqualTo("AB");
        assertThat(PlainTextLengthValidator.stripHtml("&mdash;")).isEqualTo("—");
    }

    @Test
    @DisplayName("알 수 없는 엔티티는 원문을 유지한다 - 프론트엔드와 동일")
    void 알수없는_엔티티는_유지한다() {
        assertThat(PlainTextLengthValidator.stripHtml("&unknown;")).isEqualTo("&unknown;");
    }

    @Test
    @DisplayName("&nbsp; 는 공백 1자로 센다")
    void nbsp_는_공백_한자로_센다() {
        assertThat(PlainTextLengthValidator.stripHtml("a&nbsp;b")).isEqualTo("a b");
    }

    @Test
    @DisplayName("태그만 있는 빈 본문은 길이 0이다")
    void 태그만_있으면_길이는_0이다() {
        assertThat(PlainTextLengthValidator.stripHtml("<p><br></p>")).isEmpty();
    }
}
