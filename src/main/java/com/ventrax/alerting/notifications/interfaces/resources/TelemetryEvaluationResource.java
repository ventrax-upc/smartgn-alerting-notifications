package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Summary of a telemetry evaluation")
public record TelemetryEvaluationResource(
        int alertsCreated,
        List<Long> alertIds) {
}
