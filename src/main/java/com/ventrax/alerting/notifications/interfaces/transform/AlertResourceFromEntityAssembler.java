package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.interfaces.resources.AlertResource;

public final class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {
    }

    public static AlertResource toResourceFromEntity(Alert entity) {
        return new AlertResource(
                entity.getId(),
                entity.getEventId().value(),
                entity.getDeviceId().value(),
                entity.getPropertyId().value().toString(),
                entity.getSupplyPointId().value().toString(),
                entity.getAccountId().value().toString(),
                entity.getRuleId(),
                entity.getRuleVersion(),
                entity.getRuleName(),
                entity.getMetric().name(),
                entity.getOperator().name(),
                entity.getThreshold(),
                entity.getMeasuredValue(),
                entity.getCondition(),
                entity.getSeverity().name(),
                entity.getSeverity().displayName(),
                entity.getStatus().name(),
                entity.getStatus().displayName(),
                entity.getInstructions(),
                entity.getMeasuredAt(),
                entity.getReceivedAt(),
                entity.getCreatedAt(),
                entity.getAcknowledgedAt(),
                entity.getResolvedAt());
    }
}
