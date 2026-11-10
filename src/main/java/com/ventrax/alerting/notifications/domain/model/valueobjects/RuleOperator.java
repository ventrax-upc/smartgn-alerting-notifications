package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.math.BigDecimal;

public enum RuleOperator {
    GREATER_THAN("mayor que", false),
    GREATER_THAN_OR_EQUAL("mayor o igual que", false),
    LESS_THAN("menor que", false),
    LESS_THAN_OR_EQUAL("menor o igual que", false),
    EQUALS("igual a", false),
    NOT_EQUALS("distinto de", false),
    IS_TRUE("es verdadero", true),
    IS_FALSE("es falso", true);

    private final String displayName;
    private final boolean booleanOperator;

    RuleOperator(String displayName, boolean booleanOperator) {
        this.displayName = displayName;
        this.booleanOperator = booleanOperator;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isBoolean() {
        return booleanOperator;
    }

    public boolean test(BigDecimal measured, BigDecimal threshold) {
        if (measured == null) return false;
        if (booleanOperator) {
            boolean value = measured.signum() > 0;
            return this == IS_TRUE ? value : !value;
        }
        if (threshold == null) return false;
        int comparison = measured.compareTo(threshold);
        return switch (this) {
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            case EQUALS -> comparison == 0;
            case NOT_EQUALS -> comparison != 0;
            default -> false;
        };
    }
}
