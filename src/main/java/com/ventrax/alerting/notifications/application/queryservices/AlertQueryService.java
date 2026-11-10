package com.ventrax.alerting.notifications.application.queryservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertsQuery;
import com.ventrax.alerting.shared.application.result.PagedResult;

import java.util.Optional;

public interface AlertQueryService {

    PagedResult<Alert> handle(GetAllAlertsQuery query);

    Optional<Alert> handle(GetAlertByIdQuery query);
}
