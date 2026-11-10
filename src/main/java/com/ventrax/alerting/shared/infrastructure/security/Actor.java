package com.ventrax.alerting.shared.infrastructure.security;

import java.util.UUID;

public record Actor(UUID accountId, String role) {

    public boolean isSuperAdmin() {
        return "SUPERADMIN".equalsIgnoreCase(role);
    }

    public boolean isSystem() {
        return "SYSTEM".equalsIgnoreCase(role);
    }
}
