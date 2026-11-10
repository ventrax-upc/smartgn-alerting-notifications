package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Append-only notification delivery attempt")
public record NotificationAttemptResource(
        Long id,
        int attemptNumber,
        String status,
        String providerMessageId,
        String errorCode,
        String errorMessage,
        Instant attemptedAt) {
}
