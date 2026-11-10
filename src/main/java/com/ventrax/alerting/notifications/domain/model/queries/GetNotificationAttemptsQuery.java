package com.ventrax.alerting.notifications.domain.model.queries;

public record GetNotificationAttemptsQuery(Long notificationId) {

    public GetNotificationAttemptsQuery {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("notification.error.notFound");
        }
    }
}
