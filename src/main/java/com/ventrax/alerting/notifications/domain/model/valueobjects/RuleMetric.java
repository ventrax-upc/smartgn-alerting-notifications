package com.ventrax.alerting.notifications.domain.model.valueobjects;

public enum RuleMetric {
    CONSUMPTION("Consumo", false),
    PRESSURE("Presión", false),
    GAS_PRESENCE("Presencia de gas", true),
    VALVE_STATE("Estado de válvula", true);

    private final String displayName;
    private final boolean booleanMetric;

    RuleMetric(String displayName, boolean booleanMetric) {
        this.displayName = displayName;
        this.booleanMetric = booleanMetric;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isBoolean() {
        return booleanMetric;
    }
}
