package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum NotificationType {
    CRITICAL_ALERT("Alerta crítica"),
    OPERATIONAL_NOTICE("Aviso operativo");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
