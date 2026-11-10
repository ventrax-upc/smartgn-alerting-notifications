package com.ventrax.alerting.notifications.domain.model.queries;

public record GetNotificationByIdQuery(Long notificationId) {

    public GetNotificationByIdQuery {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("notification.error.notFound");
        }
    }
}
