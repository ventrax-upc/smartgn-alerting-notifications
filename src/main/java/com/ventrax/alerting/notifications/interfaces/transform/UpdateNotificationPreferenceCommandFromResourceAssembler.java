package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.interfaces.resources.UpdateNotificationPreferenceResource;

public final class UpdateNotificationPreferenceCommandFromResourceAssembler {

    private UpdateNotificationPreferenceCommandFromResourceAssembler() {
    }

    public static UpdateNotificationPreferenceCommand toCommandFromResource(
            UpdateNotificationPreferenceResource resource) {
        return new UpdateNotificationPreferenceCommand(
                new AccountId(resource.accountId()),
                resource.notificationType(),
                resource.channel(),
                resource.recipient(),
                resource.active());
    }
}
