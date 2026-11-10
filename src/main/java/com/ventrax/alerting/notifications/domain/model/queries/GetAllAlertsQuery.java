package com.ventrax.alerting.notifications.domain.model.queries;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;

import java.time.Instant;

public record GetAllAlertsQuery(
        AccountId accountId,
        SupplyPointId supplyPointId,
        AlertStatus status,
        AlertSeverity severity,
        Instant from,
        Instant to,
        int page,
        int size) {

    public GetAllAlertsQuery {
        if (page < 0) {
            throw new IllegalArgumentException("errors.found");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("errors.found");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("errors.found");
        }
    }
}
