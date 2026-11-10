package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.interfaces.resources.AlertRuleResource;

public final class AlertRuleResourceFromEntityAssembler {

    private AlertRuleResourceFromEntityAssembler() {
    }

    public static AlertRuleResource toResourceFromEntity(AlertRule entity) {
        return new AlertRuleResource(
                entity.getId(),
                entity.getName(),
                entity.getMetric().name(),
                entity.getMetric().displayName(),
                entity.getOperator().name(),
                entity.getOperator().displayName(),
                entity.getThreshold() == null ? null : entity.getThreshold().value(),
                entity.getSeverity().name(),
                entity.getSeverity().displayName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
