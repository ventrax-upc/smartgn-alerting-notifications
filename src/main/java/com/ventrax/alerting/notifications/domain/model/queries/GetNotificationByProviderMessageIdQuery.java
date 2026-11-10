package com.ventrax.alerting.notifications.domain.model.queries;

public record GetNotificationByProviderMessageIdQuery(String providerMessageId) {

    public GetNotificationByProviderMessageIdQuery {
        if (providerMessageId == null || providerMessageId.isBlank()) {
            throw new IllegalArgumentException("notification.error.receipt.invalid");
        }
    }
}
