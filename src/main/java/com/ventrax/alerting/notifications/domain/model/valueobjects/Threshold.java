package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.math.BigDecimal;

public record Threshold(BigDecimal value) {

    private static final int MAX_SCALE = 4;
    private static final int MAX_PRECISION = 18;

    public Threshold {
        if (value == null) {
            throw new IllegalArgumentException("alert.rule.error.threshold.required");
        }
        if (value.scale() > MAX_SCALE || value.precision() > MAX_PRECISION) {
            throw new IllegalArgumentException("alert.rule.error.threshold.invalid");
        }
    }
}
