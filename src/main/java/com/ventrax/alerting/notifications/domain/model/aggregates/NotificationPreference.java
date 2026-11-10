package com.ventrax.alerting.notifications.domain.model.aggregates;

import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import com.ventrax.alerting.notifications.domain.model.valueobjects.Recipient;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters.AccountIdAttributeConverter;
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
                columnNames = {"account_id", "notification_type"},
                name = NotificationPreference.ACCOUNT_TYPE_UNIQUE_CONSTRAINT)
})
public class NotificationPreference {

    public static final String ACCOUNT_TYPE_UNIQUE_CONSTRAINT =
            "uk_notification_preference_account_type";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AccountIdAttributeConverter.class)
    private AccountId accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 320)
    @Convert(converter = RecipientAttributeConverter.class)
    private Recipient recipient;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    protected NotificationPreference() {
        // Required by JPA
    }

    public NotificationPreference(UpdateNotificationPreferenceCommand command) {
        this.accountId = command.accountId();
        this.notificationType = command.notificationType();
        this.channel = command.channel();
        this.recipient = new Recipient(command.recipient());
        this.active = command.active() == null || command.active();
        this.recipient.validateFor(this.channel);
    }

    public void update(UpdateNotificationPreferenceCommand command) {
        this.channel = command.channel();
        this.recipient = new Recipient(command.recipient());
        this.active = command.active() == null ? this.active : command.active();
        this.recipient.validateFor(this.channel);
    }
}
