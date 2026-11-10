package com.ventrax.alerting.notifications.domain.model.entities;

import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"notification_id", "attempt_number"},
                name = NotificationAttempt.NOTIFICATION_ATTEMPT_UNIQUE_CONSTRAINT)
})
public class NotificationAttempt {

    public static final String NOTIFICATION_ATTEMPT_UNIQUE_CONSTRAINT =
            "uk_notification_attempt_number";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long notificationId;

    @Column(nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(length = 200)
    private String providerMessageId;

    @Column(length = 80)
    private String errorCode;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant attemptedAt;

    protected NotificationAttempt() {
        // Required by JPA
    }

    public NotificationAttempt(Long notificationId, int attemptNumber, DeliveryStatus status,
                               String providerMessageId, String errorCode, String errorMessage) {
        this.notificationId = notificationId;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.providerMessageId = providerMessageId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.attemptedAt = Instant.now();
    }
}
