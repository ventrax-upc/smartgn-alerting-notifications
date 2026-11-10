package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import org.springframework.http.ResponseEntity;

import java.util.List;

public final class ResponseEntityFromNotificationPreferenceQueryResultAssembler {

    private ResponseEntityFromNotificationPreferenceQueryResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromList(
            List<NotificationPreference> preferences) {
        return ResponseEntity.ok(preferences.stream()
                .map(NotificationPreferenceResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
