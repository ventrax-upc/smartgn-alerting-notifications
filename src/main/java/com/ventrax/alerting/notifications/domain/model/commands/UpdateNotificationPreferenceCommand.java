package com.ventrax.alerting.notifications.domain.model.commands;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;

public record UpdateNotificationPreferenceCommand(
        AccountId accountId,
        NotificationType notificationType,
        NotificationChannel channel,
        String recipient,
        Boolean active) {

    public UpdateNotificationPreferenceCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("telemetry.error.accountId.invalid");
        }
        if (notificationType == null) {
            throw new IllegalArgumentException("notification.preference.error.type.invalid");
        }
        if (channel == null) {
            throw new IllegalArgumentException("notification.preference.error.channel.invalid");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("notification.preference.error.recipient.notBlank");
        }
        if (recipient.length() > 320) {
            throw new IllegalArgumentException("notification.preference.error.recipient.size");
        }
    }
}
