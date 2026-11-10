package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.RecordNotificationReceiptCommand;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationReceiptResource;

public final class RecordNotificationReceiptCommandFromResourceAssembler {

    private RecordNotificationReceiptCommandFromResourceAssembler() {
    }

    public static RecordNotificationReceiptCommand toCommandFromResource(
            NotificationReceiptResource resource) {
        return new RecordNotificationReceiptCommand(
                resource.providerMessageId(),
                resource.status(),
                resource.errorCode(),
                resource.errorMessage());
    }
}
