package com.ventrax.alerting.notifications.domain.model.commands;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;

import java.math.BigDecimal;

public record CreateAlertRuleCommand(
        String name,
        RuleMetric metric,
        RuleOperator operator,
        BigDecimal threshold,
        AlertSeverity severity,
        String description,
        Boolean active) {

    public CreateAlertRuleCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("alert.rule.error.name.notBlank");
        }
        if (name.length() > 120) {
            throw new IllegalArgumentException("alert.rule.error.name.size");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("alert.rule.error.description.size");
        }
        if (metric == null) {
            throw new IllegalArgumentException("alert.rule.error.metric.invalid");
        }
        if (operator == null) {
            throw new IllegalArgumentException("alert.rule.error.operator.invalid");
        }
        if (severity == null) {
            throw new IllegalArgumentException("alert.rule.error.severity.invalid");
        }
    }
}
