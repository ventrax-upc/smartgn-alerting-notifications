package com.ventrax.alerting.notifications.interfaces.resources;

import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Asynchronous provider receipt")
public record NotificationReceiptResource(
        @NotBlank(message = "{notification.error.receipt.invalid}")
        @Size(max = 200, message = "{notification.error.receipt.invalid}")
        String providerMessageId,

        @NotNull(message = "{notification.error.receipt.invalid}")
        DeliveryStatus status,

        @Size(max = 80, message = "{notification.error.receipt.invalid}")
        String errorCode,

        @Size(max = 500, message = "{notification.error.receipt.invalid}")
        String errorMessage) {
}
