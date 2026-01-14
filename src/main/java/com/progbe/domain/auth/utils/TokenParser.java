package com.progbe.domain.auth.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.user.dto.SocialTokens;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;

public class TokenParser {
    private TokenParser() {
        throw new CustomException(ErrorCode.CONSTRUCTION_NOT_ALLOWED);
    }

    public static SocialTokens parseTokens(String response, ObjectMapper objectMapper) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;
            return new SocialTokens(accessToken, refreshToken);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}