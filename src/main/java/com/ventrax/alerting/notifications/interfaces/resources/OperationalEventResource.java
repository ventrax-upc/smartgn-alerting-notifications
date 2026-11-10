package com.ventrax.alerting.notifications.interfaces.resources;

import com.ventrax.alerting.notifications.domain.model.valueobjects.OperationalEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Operational event that must produce a notification (US51)")
public record OperationalEventResource(
        @NotBlank(message = "{telemetry.error.eventId.notBlank}")
        String eventId,

        @NotNull(message = "{telemetry.error.accountId.invalid}")
        UUID accountId,

        @NotNull(message = "{notification.preference.error.type.invalid}")
        OperationalEventType eventType,

        @NotBlank(message = "{notification.error.subject.notBlank}")
        @Size(max = 200, message = "{notification.error.subject.size}")
        String subject,

        @NotBlank(message = "{notification.error.body.notBlank}")
        String body) {
}
