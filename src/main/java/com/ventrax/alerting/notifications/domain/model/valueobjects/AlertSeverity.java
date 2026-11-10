package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum AlertSeverity {
    INFO("Informativa"),
    WARNING("Advertencia"),
    CRITICAL("Crítica");

    private final String displayName;

    AlertSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }
}
