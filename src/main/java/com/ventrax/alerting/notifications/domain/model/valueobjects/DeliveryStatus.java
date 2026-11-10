package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum DeliveryStatus {
    PENDING("Pendiente"),
    ACCEPTED("Aceptado"),
    DELIVERED("Entregado"),
    FAILED("Fallido"),
    INDETERMINATE("Indeterminado");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == INDETERMINATE;
    }
}
