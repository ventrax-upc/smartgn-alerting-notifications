package com.ventrax.alerting.notifications.application.queryservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationPreferencesByAccountQuery;

import java.util.List;

public interface NotificationPreferenceQueryService {

    List<NotificationPreference> handle(GetNotificationPreferencesByAccountQuery query);
}
