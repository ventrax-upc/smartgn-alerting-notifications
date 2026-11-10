package com.ventrax.alerting.notifications.domain.model.commands;

import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;

public record RecordNotificationReceiptCommand(
        String providerMessageId,
        DeliveryStatus status,
        String errorCode,
        String errorMessage) {

    public RecordNotificationReceiptCommand {
        if (providerMessageId == null || providerMessageId.isBlank()) {
            throw new IllegalArgumentException("notification.error.receipt.invalid");
        }
        if (status == null || status == DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("notification.error.receipt.invalid");
        }
    }
}
