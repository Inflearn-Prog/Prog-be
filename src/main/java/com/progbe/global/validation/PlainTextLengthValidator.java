package com.progbe.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextLengthValidator implements ConstraintValidator<PlainTextLength, String> {

    private static final Pattern TAG = Pattern.compile("<[^>]*>?");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(\\d+);");
    private static final Pattern HEX_ENTITY = Pattern.compile("&#[xX]([0-9a-fA-F]+);");
    private static final Pattern NAMED_ENTITY = Pattern.compile("&([a-zA-Z]+);");

    private static final Map<String, String> NAMED_ENTITIES = Map.ofEntries(
            Map.entry("nbsp", " "),
            Map.entry("amp", "&"),
            Map.entry("lt", "<"),
            Map.entry("gt", ">"),
            Map.entry("quot", "\""),
            Map.entry("apos", "'"),
            Map.entry("mdash", "—"),
            Map.entry("ndash", "–"),
            Map.entry("hellip", "…"),
            Map.entry("middot", "·"),
            Map.entry("bull", "•"),
            Map.entry("lsquo", "‘"),
            Map.entry("rsquo", "’"),
            Map.entry("ldquo", "“"),
            Map.entry("rdquo", "”"),
            Map.entry("copy", "©"),
            Map.entry("reg", "®"),
            Map.entry("trade", "™"),
            Map.entry("deg", "°")
    );

    private int max;

    @Override
    public void initialize(PlainTextLength constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return stripHtml(value).length() <= max;
    }

    public static String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String result = TAG.matcher(html).replaceAll("");
        result = replaceNumericEntities(result, NUMERIC_ENTITY, 10);
        result = replaceNumericEntities(result, HEX_ENTITY, 16);
        result = replaceNamedEntities(result);
        return result.trim();
    }

    private static String replaceNumericEntities(String input, Pattern pattern, int radix) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(toCodePoint(matcher.group(1), radix)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String replaceNamedEntities(String input) {
        Matcher matcher = NAMED_ENTITY.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement = NAMED_ENTITIES.getOrDefault(
                    matcher.group(1).toLowerCase(), matcher.group());
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String toCodePoint(String digits, int radix) {
        try {
            int code = Integer.parseInt(digits, radix);
            if (code < 0 || code > 0x10FFFF) {
                return "";
            }
            return new String(Character.toChars(code));
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
