package com.ventrax.alerting.notifications.application.commandservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.commands.CreateOperationalNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.DispatchNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.RecordNotificationReceiptCommand;
import com.ventrax.alerting.shared.application.result.Result;

import java.util.List;

public interface NotificationCommandService {

    Result<List<Notification>, NotificationCommandFailure> handle(
            CreateOperationalNotificationCommand command);

    Result<Notification, NotificationCommandFailure> handle(DispatchNotificationCommand command);

    Result<Notification, NotificationCommandFailure> handle(
            RecordNotificationReceiptCommand command);
}
