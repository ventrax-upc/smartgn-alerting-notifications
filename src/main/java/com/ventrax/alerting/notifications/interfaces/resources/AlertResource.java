package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Alert representation")
public record AlertResource(
        Long id,
        String eventId,
        String deviceId,
        String propertyId,
        String supplyPointId,
        String accountId,
        Long ruleId,
        int ruleVersion,
        String ruleName,
        String metric,
        String operator,
        BigDecimal threshold,
        BigDecimal measuredValue,
        String condition,
        String severity,
        String severityLabel,
        String status,
        String statusLabel,
        String instructions,
        Instant measuredAt,
        Instant receivedAt,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt) {
}
