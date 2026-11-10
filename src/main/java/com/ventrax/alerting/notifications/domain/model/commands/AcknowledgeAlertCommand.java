package com.ventrax.alerting.notifications.domain.model.commands;

public record AcknowledgeAlertCommand(Long alertId) {

    public AcknowledgeAlertCommand {
        if (alertId == null || alertId <= 0) {
            throw new IllegalArgumentException("alert.error.notFound");
        }
    }
}
