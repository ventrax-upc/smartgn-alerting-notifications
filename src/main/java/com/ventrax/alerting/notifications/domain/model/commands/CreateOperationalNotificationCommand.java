package com.ventrax.alerting.notifications.domain.model.commands;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.OperationalEventType;

public record CreateOperationalNotificationCommand(
        EventId eventId,
        AccountId accountId,
        OperationalEventType eventType,
        String subject,
        String body) {

    public CreateOperationalNotificationCommand {
        if (eventId == null) {
            throw new IllegalArgumentException("telemetry.error.eventId.notBlank");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("telemetry.error.accountId.invalid");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("notification.preference.error.type.invalid");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("notification.error.subject.notBlank");
        }
        if (subject.length() > 200) {
            throw new IllegalArgumentException("notification.error.subject.size");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("notification.error.body.notBlank");
        }
    }
}
