package com.ventrax.alerting.shared.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartgn.security")
public record SecurityProperties(
        boolean enabled,
        String internalToken,
        String allowedOrigin) {
}
