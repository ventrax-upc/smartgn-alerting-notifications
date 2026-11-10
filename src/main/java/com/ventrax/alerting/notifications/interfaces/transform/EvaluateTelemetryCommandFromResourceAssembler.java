package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.EvaluateTelemetryCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeviceId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.PropertyId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;
import com.ventrax.alerting.notifications.interfaces.resources.TelemetryEventResource;

public final class EvaluateTelemetryCommandFromResourceAssembler {

    private EvaluateTelemetryCommandFromResourceAssembler() {
    }

    public static EvaluateTelemetryCommand toCommandFromResource(
            TelemetryEventResource resource) {
        return new EvaluateTelemetryCommand(
                new EventId(resource.eventId()),
                new DeviceId(resource.deviceId()),
                new PropertyId(resource.propertyId()),
                new SupplyPointId(resource.supplyPointId()),
                new AccountId(resource.accountId()),
                resource.measuredAt(),
                resource.receivedAt(),
                resource.consumptionM3(),
                resource.pressureKpa(),
                resource.gasPresence(),
                resource.valveOpen(),
                resource.recipientEmail(),
                resource.recipientPhone());
    }
}
