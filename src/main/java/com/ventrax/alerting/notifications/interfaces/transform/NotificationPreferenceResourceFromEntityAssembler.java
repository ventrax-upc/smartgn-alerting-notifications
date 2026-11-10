package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationPreferenceResource;

public final class NotificationPreferenceResourceFromEntityAssembler {

    private NotificationPreferenceResourceFromEntityAssembler() {
    }

    public static NotificationPreferenceResource toResourceFromEntity(
            NotificationPreference entity) {
        return new NotificationPreferenceResource(
                entity.getId(),
                entity.getAccountId().value().toString(),
                entity.getNotificationType().name(),
                entity.getChannel().name(),
                entity.getChannel().displayName(),
                entity.getRecipient().value(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
