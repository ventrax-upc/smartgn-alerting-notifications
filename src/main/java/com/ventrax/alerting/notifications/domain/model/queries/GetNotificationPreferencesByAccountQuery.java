package com.ventrax.alerting.notifications.domain.model.queries;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;

public record GetNotificationPreferencesByAccountQuery(AccountId accountId) {

    public GetNotificationPreferencesByAccountQuery {
        if (accountId == null) {
            throw new IllegalArgumentException("telemetry.error.accountId.invalid");
        }
    }
}
