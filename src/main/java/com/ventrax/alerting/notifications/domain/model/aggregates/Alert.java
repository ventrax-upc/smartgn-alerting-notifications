package com.ventrax.alerting.notifications.domain.model.aggregates;

import com.ventrax.alerting.notifications.domain.model.commands.EvaluateTelemetryCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.*;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.*;
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
        @UniqueConstraint(
                columnNames = {"device_id", "event_id", "rule_id"},
                name = Alert.DEVICE_EVENT_RULE_UNIQUE_CONSTRAINT)
})
public class Alert {

    public static final String DEVICE_EVENT_RULE_UNIQUE_CONSTRAINT = "uk_alert_device_event_rule";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    @Convert(converter = EventIdAttributeConverter.class)
    private EventId eventId;

    @Column(nullable = false, length = 128)
    @Convert(converter = DeviceIdAttributeConverter.class)
    private DeviceId deviceId;

    @Column(nullable = false)
    @Convert(converter = PropertyIdAttributeConverter.class)
    private PropertyId propertyId;

    @Column(nullable = false)
    @Convert(converter = SupplyPointIdAttributeConverter.class)
    private SupplyPointId supplyPointId;

    @Column(nullable = false)
    @Convert(converter = AccountIdAttributeConverter.class)
    private AccountId accountId;

    @Column(nullable = false)
    private Long ruleId;

    @Column(nullable = false)
    private int ruleVersion;

    @Column(nullable = false, length = 120)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RuleMetric metric;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RuleOperator operator;

    @Column(precision = 18, scale = 4)
    private BigDecimal threshold;

    @Column(precision = 18, scale = 4)
    private BigDecimal measuredValue;

    @Column(nullable = false, length = 500)
    private String condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private Instant measuredAt;

    @Column(nullable = false)
    private Instant receivedAt;

    @Column
    private Instant acknowledgedAt;

    @Column
    private Instant resolvedAt;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    protected Alert() {
        // Required by JPA
    }

    public Alert(EvaluateTelemetryCommand command, AlertRule rule, BigDecimal measuredValue,
                 String condition, String instructions) {
        this.eventId = command.eventId();
        this.deviceId = command.deviceId();
        this.propertyId = command.propertyId();
        this.supplyPointId = command.supplyPointId();
        this.accountId = command.accountId();
        this.ruleId = rule.getId();
        this.ruleVersion = rule.getVersion();
        this.ruleName = rule.getName();
        this.metric = rule.getMetric();
        this.operator = rule.getOperator();
        this.threshold = rule.getThreshold() == null ? null : rule.getThreshold().value();
        this.measuredValue = measuredValue;
        this.condition = condition;
        this.severity = rule.getSeverity();
        this.status = AlertStatus.OPEN;
        this.instructions = instructions;
        this.measuredAt = command.measuredAt();
        this.receivedAt = command.receivedAt() == null ? Instant.now() : command.receivedAt();
    }

    public void acknowledge() {
        if (status != AlertStatus.OPEN) {
            throw new IllegalStateException("alert.error.status.invalid");
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
    }

    public void resolve() {
        if (status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("alert.error.status.invalid");
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
