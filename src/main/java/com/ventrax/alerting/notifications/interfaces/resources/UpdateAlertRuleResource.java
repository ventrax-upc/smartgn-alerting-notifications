package com.ventrax.alerting.notifications.interfaces.resources;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request resource used to update an alert rule")
public record UpdateAlertRuleResource(
        @NotBlank(message = "{alert.rule.error.name.notBlank}")
        @Size(max = 120, message = "{alert.rule.error.name.size}")
        String name,

        @NotNull(message = "{alert.rule.error.metric.invalid}")
        RuleMetric metric,

        @NotNull(message = "{alert.rule.error.operator.invalid}")
        RuleOperator operator,

        BigDecimal threshold,

        @NotNull(message = "{alert.rule.error.severity.invalid}")
        AlertSeverity severity,

        @Size(max = 500, message = "{alert.rule.error.description.size}")
        String description,

        Boolean active) {
}
