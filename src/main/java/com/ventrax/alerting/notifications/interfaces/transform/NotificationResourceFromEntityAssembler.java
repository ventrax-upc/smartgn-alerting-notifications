package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationResource;

public final class NotificationResourceFromEntityAssembler {

    private NotificationResourceFromEntityAssembler() {
    }

    public static NotificationResource toResourceFromEntity(Notification entity) {
        return new NotificationResource(
                entity.getId(),
                entity.getAlertId(),
                entity.getSourceEventId() == null ? null : entity.getSourceEventId().value(),
                entity.getAccountId().value().toString(),
                entity.getNotificationType().name(),
                entity.getOperationalEventType() == null
                        ? null
                        : entity.getOperationalEventType().name(),
                entity.getChannel().name(),
                entity.getRecipient().value(),
                entity.getSubject(),
                entity.getBody(),
                entity.getStatus().name(),
                entity.getStatus().displayName(),
                entity.getAttempts(),
                entity.getProviderMessageId(),
                entity.getLastError(),
                entity.getNextAttemptAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
