package com.ventrax.alerting.notifications.application.queryservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationAttemptsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationByProviderMessageIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationsByAlertQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetPendingNotificationsQuery;
import com.ventrax.alerting.shared.application.result.PagedResult;

import java.util.List;
import java.util.Optional;

public interface NotificationQueryService {

    PagedResult<Notification> handle(GetAllNotificationsQuery query);

    List<Notification> handle(GetNotificationsByAlertQuery query);

    List<Notification> handle(GetPendingNotificationsQuery query);

    Optional<Notification> handle(GetNotificationByIdQuery query);

    Optional<Notification> handle(GetNotificationByProviderMessageIdQuery query);

    List<NotificationAttempt> handle(GetNotificationAttemptsQuery query);
}
