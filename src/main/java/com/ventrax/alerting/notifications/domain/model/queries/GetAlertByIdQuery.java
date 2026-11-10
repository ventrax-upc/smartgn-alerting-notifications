package com.ventrax.alerting.notifications.domain.model.queries;

public record GetAlertByIdQuery(Long alertId) {

    public GetAlertByIdQuery {
        if (alertId == null || alertId <= 0) {
            throw new IllegalArgumentException("alert.error.notFound");
        }
    }
}
