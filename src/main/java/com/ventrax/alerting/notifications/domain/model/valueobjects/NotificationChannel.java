package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum NotificationChannel {
    EMAIL("Correo electrónico"),
    SMS("SMS"),
    WHATSAPP("WhatsApp");

    private final String displayName;

    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
