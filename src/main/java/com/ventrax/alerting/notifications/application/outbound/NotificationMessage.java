package com.ventrax.alerting.notifications.application.outbound;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;

/**
 * Immutable message handed to a channel strategy.
 */
public record NotificationMessage(
        Long notificationId,
        Long alertId,
        NotificationType notificationType,
        NotificationChannel channel,
        String recipient,
        String subject,
        String body) {
}
