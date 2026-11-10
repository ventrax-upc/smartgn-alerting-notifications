package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Notification outbox and delivery state")
public record NotificationResource(
        Long id,
        Long alertId,
        String sourceEventId,
        String accountId,
        String notificationType,
        String operationalEventType,
        String channel,
        String recipient,
        String subject,
        String body,
        String status,
        String statusLabel,
        int attempts,
        String providerMessageId,
        String lastError,
        Instant nextAttemptAt,
        Instant createdAt,
        Instant updatedAt) {
}
