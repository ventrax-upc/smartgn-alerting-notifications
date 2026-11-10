package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Notification preference for an account and notification type")
public record NotificationPreferenceResource(
        Long id,
        String accountId,
        String notificationType,
        String channel,
        String channelLabel,
        String recipient,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
