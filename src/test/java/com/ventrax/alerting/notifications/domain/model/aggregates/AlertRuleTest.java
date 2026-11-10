package com.ventrax.alerting.notifications.domain.model.aggregates;

import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import com.ventrax.alerting.notifications.domain.model.valueobjects.TelemetryReading;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AlertRuleTest {

    @Test
    void numericRuleMatchesBelowThreshold() {
        var rule = new AlertRule(new CreateAlertRuleCommand(
                "Low pressure",
                RuleMetric.PRESSURE,
                RuleOperator.LESS_THAN,
                new BigDecimal("0.5"),
                AlertSeverity.CRITICAL,
                "Pressure below the safety threshold",
                true));

        assertTrue(rule.matches(new TelemetryReading(
                null, new BigDecimal("0.4"), null, null)));
        assertFalse(rule.matches(new TelemetryReading(
                null, new BigDecimal("0.6"), null, null)));
        assertFalse(rule.matches(new TelemetryReading(
                null, null, null, null)));
    }

    @Test
    void booleanRuleMatchesGasPresence() {
        var rule = new AlertRule(new CreateAlertRuleCommand(
                "Gas detected",
                RuleMetric.GAS_PRESENCE,
                RuleOperator.IS_TRUE,
                null,
                AlertSeverity.CRITICAL,
                "Gas presence",
                true));

        assertTrue(rule.matches(new TelemetryReading(null, null, true, null)));
        assertFalse(rule.matches(new TelemetryReading(null, null, false, null)));
    }

    @Test
    void rejectsNumericOperatorForBooleanMetric() {
        assertThrows(IllegalArgumentException.class, () -> new AlertRule(
                new CreateAlertRuleCommand(
                        "Invalid",
                        RuleMetric.GAS_PRESENCE,
                        RuleOperator.GREATER_THAN,
                        BigDecimal.ONE,
                        AlertSeverity.CRITICAL,
                        "Invalid combination",
                        true)));
    }

    @Test
    void describesNumericCondition() {
        var rule = new AlertRule(new CreateAlertRuleCommand(
                "Low pressure",
                RuleMetric.PRESSURE,
                RuleOperator.LESS_THAN,
                new BigDecimal("0.5"),
                AlertSeverity.CRITICAL,
                "Pressure below the safety threshold",
                true));

        var condition = rule.describeCondition(new BigDecimal("0.4"));
        assertTrue(condition.contains("Presión"));
        assertTrue(condition.contains("0.5"));
        assertTrue(condition.contains("0.4"));
    }

    @Test
    void updatePreservesActiveWhenNotProvided() {
        var rule = new AlertRule(new CreateAlertRuleCommand(
                "Disabled rule",
                RuleMetric.PRESSURE,
                RuleOperator.LESS_THAN,
                new BigDecimal("0.5"),
                AlertSeverity.WARNING,
                "Disabled rule",
                false));

        rule.update(new UpdateAlertRuleCommand(
                1L,
                "Disabled rule updated",
                RuleMetric.PRESSURE,
                RuleOperator.LESS_THAN,
                new BigDecimal("0.4"),
                AlertSeverity.CRITICAL,
                "Updated",
                null));

        assertFalse(rule.isActive());
        assertEquals("Disabled rule updated", rule.getName());
    }
}
