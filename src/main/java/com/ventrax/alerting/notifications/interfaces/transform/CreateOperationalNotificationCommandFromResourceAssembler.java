package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.CreateOperationalNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.interfaces.resources.OperationalEventResource;

public final class CreateOperationalNotificationCommandFromResourceAssembler {

    private CreateOperationalNotificationCommandFromResourceAssembler() {
    }

    public static CreateOperationalNotificationCommand toCommandFromResource(
            OperationalEventResource resource) {
        return new CreateOperationalNotificationCommand(
                new EventId(resource.eventId()),
                new AccountId(resource.accountId()),
                resource.eventType(),
                resource.subject(),
                resource.body());
    }
}
