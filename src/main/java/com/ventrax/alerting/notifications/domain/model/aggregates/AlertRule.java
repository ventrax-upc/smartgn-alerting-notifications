package com.ventrax.alerting.notifications.domain.model.aggregates;

import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import com.ventrax.alerting.notifications.domain.model.valueobjects.TelemetryReading;
import com.ventrax.alerting.notifications.domain.model.valueobjects.Threshold;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.ThresholdAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"}, name = AlertRule.NAME_UNIQUE_CONSTRAINT)
})
public class AlertRule {

    public static final String NAME_UNIQUE_CONSTRAINT = "uk_alert_rule_name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RuleMetric metric;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RuleOperator operator;

    @Column(precision = 18, scale = 4)
    @Convert(converter = ThresholdAttributeConverter.class)
    private Threshold threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    protected AlertRule() {
        // Required by JPA
    }

    public AlertRule(CreateAlertRuleCommand command) {
        this.name = command.name();
        this.metric = command.metric();
        this.operator = command.operator();
        this.threshold = command.threshold() == null ? null : new Threshold(command.threshold());
        this.severity = command.severity();
        this.description = command.description();
        this.active = command.active() == null || command.active();
        this.version = 1;
        validateMetricOperator();
    }

    public void update(UpdateAlertRuleCommand command) {
        this.name = command.name();
        this.metric = command.metric();
        this.operator = command.operator();
        this.threshold = command.threshold() == null ? null : new Threshold(command.threshold());
        this.severity = command.severity();
        this.description = command.description();
        this.active = command.active() == null ? this.active : command.active();
        this.version = this.version + 1;
        validateMetricOperator();
    }

    public boolean matches(TelemetryReading reading) {
        if (!active || reading == null) return false;
        var measured = reading.valueFor(metric);
        if (measured == null) return false;
        return operator.test(measured, threshold == null ? null : threshold.value());
    }

    public String describeCondition(BigDecimal measuredValue) {
        if (measuredValue == null) {
            return metric.displayName() + " sin valor medido";
        }
        return switch (metric) {
            case GAS_PRESENCE -> measuredValue.signum() > 0
                    ? "Presencia de gas detectada"
                    : "Presencia de gas no detectada";
            case VALVE_STATE -> measuredValue.signum() > 0
                    ? "Válvula abierta"
                    : "Válvula cerrada";
            case PRESSURE -> "Presión " + operator.displayName() + " "
                    + threshold.value().toPlainString() + " kPa (valor medido: "
                    + measuredValue.toPlainString() + " kPa)";
            case CONSUMPTION -> "Consumo " + operator.displayName() + " "
                    + threshold.value().toPlainString() + " m³ (valor medido: "
                    + measuredValue.toPlainString() + " m³)";
        };
    }

    private void validateMetricOperator() {
        if (metric.isBoolean() && !operator.isBoolean()) {
            throw new IllegalArgumentException("alert.rule.error.operator.invalid");
        }
        if (!metric.isBoolean() && operator.isBoolean()) {
            throw new IllegalArgumentException("alert.rule.error.operator.invalid");
        }
        if (metric.isBoolean() && threshold != null) {
            throw new IllegalArgumentException("alert.rule.error.threshold.notAllowed");
        }
        if (!metric.isBoolean() && threshold == null) {
            throw new IllegalArgumentException("alert.rule.error.threshold.required");
        }
    }
}
