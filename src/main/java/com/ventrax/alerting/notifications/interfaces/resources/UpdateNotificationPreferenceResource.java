package com.ventrax.alerting.notifications.interfaces.resources;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request resource used to upsert a notification preference")
public record UpdateNotificationPreferenceResource(
        @NotNull(message = "{telemetry.error.accountId.invalid}")
        UUID accountId,

        @NotNull(message = "{notification.preference.error.type.invalid}")
        NotificationType notificationType,

        @NotNull(message = "{notification.preference.error.channel.invalid}")
        NotificationChannel channel,

        @NotBlank(message = "{notification.preference.error.recipient.notBlank}")
        @Size(max = 320, message = "{notification.preference.error.recipient.size}")
        String recipient,

        Boolean active) {
}
