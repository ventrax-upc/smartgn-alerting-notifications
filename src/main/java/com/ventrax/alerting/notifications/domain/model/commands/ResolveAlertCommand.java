package com.ventrax.alerting.notifications.domain.model.commands;

public record ResolveAlertCommand(Long alertId) {

    public ResolveAlertCommand {
        if (alertId == null || alertId <= 0) {
            throw new IllegalArgumentException("alert.error.notFound");
        }
    }
}
