package com.ventrax.alerting.notifications.domain.model.queries;

public record GetNotificationsByAlertQuery(Long alertId) {

    public GetNotificationsByAlertQuery {
        if (alertId == null || alertId <= 0) {
            throw new IllegalArgumentException("alert.error.notFound");
        }
    }
}
