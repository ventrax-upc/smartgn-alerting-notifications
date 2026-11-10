package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.util.UUID;

public record SupplyPointId(UUID value) {

    public SupplyPointId {
        if (value == null) {
            throw new IllegalArgumentException("telemetry.error.supplyPointId.invalid");
        }
    }
}
