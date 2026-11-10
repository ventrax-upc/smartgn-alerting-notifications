package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.util.UUID;

public record PropertyId(UUID value) {

    public PropertyId {
        if (value == null) {
            throw new IllegalArgumentException("telemetry.error.propertyId.invalid");
        }
    }
}
