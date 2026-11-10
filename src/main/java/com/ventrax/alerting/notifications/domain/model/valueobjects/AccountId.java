package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        if (value == null) {
            throw new IllegalArgumentException("telemetry.error.accountId.invalid");
        }
    }
}
