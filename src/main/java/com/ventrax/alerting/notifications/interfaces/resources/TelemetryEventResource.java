package com.ventrax.alerting.notifications.interfaces.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Normalized telemetry event consumed from Telemetry & Data Ingestion")
public record TelemetryEventResource(
        @NotBlank(message = "{telemetry.error.eventId.notBlank}")
        @Schema(description = "Unique telemetry event identifier", example = "evt-2026-0001")
        String eventId,

        @NotBlank(message = "{telemetry.error.deviceId.notBlank}")
        @Schema(description = "IoT device identifier", example = "dev-001")
        String deviceId,

        @NotNull(message = "{telemetry.error.propertyId.invalid}")
        @Schema(description = "Property identifier")
        UUID propertyId,

        @NotNull(message = "{telemetry.error.supplyPointId.invalid}")
        @Schema(description = "Supply point identifier")
        UUID supplyPointId,

        @NotNull(message = "{telemetry.error.accountId.invalid}")
        @Schema(description = "Principal account identifier")
        UUID accountId,

        @NotNull(message = "{alert.error.measuredAt.invalid}")
        @Schema(description = "Instant reported by the sensor")
        Instant measuredAt,

        @Schema(description = "Instant at which the broker received the reading")
        Instant receivedAt,

        @Schema(description = "Consumption in cubic meters", example = "12.5")
        BigDecimal consumptionM3,

        @Schema(description = "Pressure in kPa", example = "0.4")
        BigDecimal pressureKpa,

        @Schema(description = "Gas presence reported by the sensor", example = "true")
        Boolean gasPresence,

        @Schema(description = "Valve state reported by the sensor", example = "false")
        Boolean valveOpen,

        @Email(message = "{notification.preference.error.recipient.invalid}")
        @Schema(description = "Optional contact email projection from IAM")
        String recipientEmail,

        @Schema(description = "Optional contact phone projection from IAM")
        String recipientPhone) {
}
