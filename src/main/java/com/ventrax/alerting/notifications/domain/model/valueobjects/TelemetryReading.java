package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.math.BigDecimal;

public record TelemetryReading(
        BigDecimal consumptionM3,
        BigDecimal pressureKpa,
        Boolean gasPresence,
        Boolean valveOpen) {

    public BigDecimal valueFor(RuleMetric metric) {
        if (metric == null) return null;
        return switch (metric) {
            case CONSUMPTION -> consumptionM3;
            case PRESSURE -> pressureKpa;
            case GAS_PRESENCE -> gasPresence == null ? null : (gasPresence ? BigDecimal.ONE : BigDecimal.ZERO);
            case VALVE_STATE -> valveOpen == null ? null : (valveOpen ? BigDecimal.ONE : BigDecimal.ZERO);
        };
    }

    public boolean hasAnyMetric() {
        return consumptionM3 != null || pressureKpa != null || gasPresence != null || valveOpen != null;
    }
}
