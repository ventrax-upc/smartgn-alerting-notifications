package com.ventrax.alerting.notifications.domain.model.commands;

public record DispatchNotificationCommand(Long notificationId) {

    public DispatchNotificationCommand {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("notification.error.notFound");
        }
    }
}
