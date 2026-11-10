package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationAttemptResource;

public final class NotificationAttemptResourceFromEntityAssembler {

    private NotificationAttemptResourceFromEntityAssembler() {
    }

    public static NotificationAttemptResource toResourceFromEntity(NotificationAttempt entity) {
        return new NotificationAttemptResource(
                entity.getId(),
                entity.getAttemptNumber(),
                entity.getStatus().name(),
                entity.getProviderMessageId(),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                entity.getAttemptedAt());
    }
}
