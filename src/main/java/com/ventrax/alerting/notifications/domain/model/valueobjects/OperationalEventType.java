package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum OperationalEventType {
    ASSIGNMENT_CREATED("Asignación registrada"),
    APPOINTMENT_CHANGED("Cita modificada"),
    PAYMENT_CONFIRMED("Pago confirmado"),
    INVOICE_ISSUED("Boleta emitida"),
    INSTALLATION_STATUS_CHANGED("Estado de instalación actualizado");

    private final String displayName;

    OperationalEventType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
