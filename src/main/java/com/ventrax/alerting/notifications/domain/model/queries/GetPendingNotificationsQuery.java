package com.ventrax.alerting.notifications.domain.model.queries;

public record GetPendingNotificationsQuery(int limit) {

    public GetPendingNotificationsQuery {
        if (limit < 1 || limit > 500) {
            throw new IllegalArgumentException("errors.found");
        }
    }
}
