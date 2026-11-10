package com.ventrax.alerting.notifications.interfaces.resources;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request resource used to create an alert rule")
public record CreateAlertRuleResource(
        @NotBlank(message = "{alert.rule.error.name.notBlank}")
        @Size(max = 120, message = "{alert.rule.error.name.size}")
        @Schema(description = "Unique rule name", example = "Critical low pressure")
        String name,

        @NotNull(message = "{alert.rule.error.metric.invalid}")
        @Schema(description = "Telemetry metric evaluated by the rule", example = "PRESSURE")
        RuleMetric metric,

        @NotNull(message = "{alert.rule.error.operator.invalid}")
        @Schema(description = "Comparison operator", example = "LESS_THAN")
        RuleOperator operator,

        @Schema(description = "Numeric threshold; required for numeric metrics", example = "0.5")
        BigDecimal threshold,

        @NotNull(message = "{alert.rule.error.severity.invalid}")
        @Schema(description = "Alert severity", example = "CRITICAL")
        AlertSeverity severity,

        @Size(max = 500, message = "{alert.rule.error.description.size}")
        @Schema(description = "Human-readable rule description")
        String description,

        @Schema(description = "Whether the rule is active", example = "true")
        Boolean active) {
}
