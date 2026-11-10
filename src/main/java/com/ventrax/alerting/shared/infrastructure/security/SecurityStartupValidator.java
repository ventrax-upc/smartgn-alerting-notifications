package com.ventrax.alerting.shared.infrastructure.security;

import org.springframework.stereotype.Component;

/**
 * Fails fast when security is enabled with the development token.
 */
@Component
public class SecurityStartupValidator {

    public SecurityStartupValidator(SecurityProperties properties) {
        if (properties.enabled() && isInsecure(properties.internalToken())) {
            throw new IllegalStateException(
                    "INTERNAL_SERVICE_TOKEN must be configured with a non-default value "
                            + "when smartgn.security.enabled=true");
        }
    }

    private boolean isInsecure(String token) {
        return token == null || token.isBlank() || token.contains("change-me");
    }
}
