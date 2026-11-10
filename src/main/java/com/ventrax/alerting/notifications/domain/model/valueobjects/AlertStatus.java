package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum AlertStatus {
    OPEN("Abierta"),
    ACKNOWLEDGED("Reconocida"),
    RESOLVED("Resuelta");

    private final String displayName;

    AlertStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
