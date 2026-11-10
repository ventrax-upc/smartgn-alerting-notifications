package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Alert rule representation")
public record AlertRuleResource(
        Long id,
        String name,
        String metric,
        String metricLabel,
        String operator,
        String operatorLabel,
        BigDecimal threshold,
        String severity,
        String severityLabel,
        String description,
        boolean active,
        int version,
        Instant createdAt,
        Instant updatedAt) {
}
