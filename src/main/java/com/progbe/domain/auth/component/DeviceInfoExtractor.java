package com.progbe.domain.auth.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class DeviceInfoExtractor {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String DEFAULT_DEVICE_INFO = "Unknown Device";

    public String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        return (userAgent != null && !userAgent.isBlank()) ? userAgent : DEFAULT_DEVICE_INFO;
    }
}
