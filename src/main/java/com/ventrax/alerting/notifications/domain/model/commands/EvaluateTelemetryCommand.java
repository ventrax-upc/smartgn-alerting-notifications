package com.ventrax.alerting.notifications.domain.model.commands;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeviceId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.PropertyId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.TelemetryReading;

import java.math.BigDecimal;
import java.time.Instant;

public record EvaluateTelemetryCommand(
        EventId eventId,
        DeviceId deviceId,
        PropertyId propertyId,
        SupplyPointId supplyPointId,
        AccountId accountId,
        Instant measuredAt,
        Instant receivedAt,
        BigDecimal consumptionM3,
        BigDecimal pressureKpa,
        Boolean gasPresence,
        Boolean valveOpen,
        String recipientEmail,
        String recipientPhone) {

    public EvaluateTelemetryCommand {
        if (eventId == null) {
            throw new IllegalArgumentException("telemetry.error.eventId.notBlank");
        }
        if (deviceId == null) {
            throw new IllegalArgumentException("telemetry.error.deviceId.notBlank");
        }
        if (propertyId == null) {
            throw new IllegalArgumentException("telemetry.error.propertyId.invalid");
        }
        if (supplyPointId == null) {
            throw new IllegalArgumentException("telemetry.error.supplyPointId.invalid");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("telemetry.error.accountId.invalid");
        }
        if (measuredAt == null) {
            throw new IllegalArgumentException("alert.error.measuredAt.invalid");
        }
    }

    public TelemetryReading reading() {
        return new TelemetryReading(consumptionM3, pressureKpa, gasPresence, valveOpen);
    }
}
