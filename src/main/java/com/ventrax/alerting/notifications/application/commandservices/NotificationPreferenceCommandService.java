package com.ventrax.alerting.notifications.application.commandservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.shared.application.result.Result;

public interface NotificationPreferenceCommandService {

    Result<NotificationPreference, NotificationPreferenceCommandFailure> handle(
            UpdateNotificationPreferenceCommand command);
}
