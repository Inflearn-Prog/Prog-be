package com.progbe.global.oauth;

import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.Builder;

import java.util.Map;

/**
 * 각 소셜에서 받아온 데이터를 통합하여 관리하는 DTO
 *
 * @param providerId 정규화된 데이터 소셜 식별 ID
 */
@SuppressWarnings("unchecked")
public record OAuth2Attributes(
        Map<String, Object> attributes,
        String nameAttributeKey,
        String providerId,
        String nickname,
        String email,
        String profileImageUrl
) {

    @Builder
    public OAuth2Attributes {
        /* nop */
    }

    public static OAuth2Attributes of(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }
        throw new CustomException(ErrorCode.INVALID_PROVIDER);
    }

    /**
     * Naver : response 필드 안에 실제 정보가 있음 (attribute = meta data, unused)
     * Kakao : kakao_account -> profile
     */
    private static OAuth2Attributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attributes.builder()
                .attributes(attributes)
                .providerId((String) response.get("id"))
                .nickname((String) response.get("name"))
                .email((String) response.get("email"))
                .profileImageUrl((String) response.get("profile_image"))
                .build();
    }

    private static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .attributes(attributes)
                .providerId(String.valueOf(attributes.get("id")))
                .nickname((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .profileImageUrl((String) profile.get("profile_image_url"))
                .build();
    }
}