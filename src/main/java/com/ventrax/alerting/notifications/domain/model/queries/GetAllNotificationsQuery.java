package com.ventrax.alerting.notifications.domain.model.queries;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;

public record GetAllNotificationsQuery(
        AccountId accountId,
        Long alertId,
        DeliveryStatus status,
        int page,
        int size) {

    public GetAllNotificationsQuery {
        if (page < 0) {
            throw new IllegalArgumentException("errors.found");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("errors.found");
        }
    }
}
