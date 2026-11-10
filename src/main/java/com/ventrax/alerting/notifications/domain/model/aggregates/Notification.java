package com.ventrax.alerting.notifications.domain.model.aggregates;

import com.ventrax.alerting.notifications.domain.model.valueobjects.*;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.AccountIdAttributeConverter;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.EventIdAttributeConverter;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.RecipientAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"alert_id", "channel", "recipient"},
                name = Notification.ALERT_CHANNEL_RECIPIENT_UNIQUE_CONSTRAINT),
        @UniqueConstraint(
                columnNames = {"source_event_id", "channel", "recipient"},
                name = Notification.SOURCE_CHANNEL_RECIPIENT_UNIQUE_CONSTRAINT)
})
public class Notification {

    public static final String ALERT_CHANNEL_RECIPIENT_UNIQUE_CONSTRAINT =
            "uk_notification_alert_channel_recipient";
    public static final String SOURCE_CHANNEL_RECIPIENT_UNIQUE_CONSTRAINT =
            "uk_notification_source_channel_recipient";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long alertId;

    @Column(length = 128)
    @Convert(converter = EventIdAttributeConverter.class)
    private EventId sourceEventId;

    @Column(nullable = false)
    @Convert(converter = AccountIdAttributeConverter.class)
    private AccountId accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private OperationalEventType operationalEventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 320)
    @Convert(converter = RecipientAttributeConverter.class)
    private Recipient recipient;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column
    private Instant nextAttemptAt;

    @Column(length = 200)
    private String providerMessageId;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    protected Notification() {
        // Required by JPA
    }

    public Notification(Alert alert, NotificationChannel channel, Recipient recipient,
                        String subject, String body) {
        this.alertId = alert.getId();
        this.accountId = alert.getAccountId();
        this.notificationType = NotificationType.CRITICAL_ALERT;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = DeliveryStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = Instant.now().minusSeconds(1);
    }

    public Notification(EventId sourceEventId, AccountId accountId,
                        OperationalEventType operationalEventType, NotificationChannel channel,
                        Recipient recipient, String subject, String body) {
        this.sourceEventId = sourceEventId;
        this.accountId = accountId;
        this.notificationType = NotificationType.OPERATIONAL_NOTICE;
        this.operationalEventType = operationalEventType;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = DeliveryStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = Instant.now().minusSeconds(1);
    }

    public void markAttemptStarted() {
        this.attempts = this.attempts + 1;
    }

    public void recordAccepted(String providerMessageId) {
        this.status = DeliveryStatus.ACCEPTED;
        this.providerMessageId = providerMessageId;
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void recordDelivered() {
        this.status = DeliveryStatus.DELIVERED;
        this.nextAttemptAt = null;
    }

    public void recordTransientFailure(String error, Instant nextAttemptAt) {
        this.status = DeliveryStatus.PENDING;
        this.lastError = error;
        this.nextAttemptAt = nextAttemptAt;
    }

    public void recordPermanentFailure(String error) {
        this.status = DeliveryStatus.FAILED;
        this.lastError = error;
        this.nextAttemptAt = null;
    }

    public void recordIndeterminate(String error) {
        this.status = DeliveryStatus.INDETERMINATE;
        this.lastError = error;
        this.nextAttemptAt = null;
    }

    public void markCircuitOpen(Instant nextAttemptAt) {
        this.status = DeliveryStatus.PENDING;
        this.lastError = "CIRCUIT_OPEN";
        this.nextAttemptAt = nextAttemptAt;
    }
}
