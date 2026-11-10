package com.ventrax.alerting.notifications.application.queryservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertRuleByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertRulesQuery;

import java.util.List;
import java.util.Optional;

public interface AlertRuleQueryService {

    List<AlertRule> handle(GetAllAlertRulesQuery query);

    Optional<AlertRule> handle(GetAlertRuleByIdQuery query);
}
