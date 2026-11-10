package com.ventrax.alerting.notifications.domain.model.queries;

public record GetAlertRuleByIdQuery(Long ruleId) {

    public GetAlertRuleByIdQuery {
        if (ruleId == null || ruleId <= 0) {
            throw new IllegalArgumentException("alert.rule.error.notFound");
        }
    }
}
