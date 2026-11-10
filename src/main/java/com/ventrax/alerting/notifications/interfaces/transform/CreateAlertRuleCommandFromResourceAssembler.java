package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.interfaces.resources.CreateAlertRuleResource;

public final class CreateAlertRuleCommandFromResourceAssembler {

    private CreateAlertRuleCommandFromResourceAssembler() {
    }

    public static CreateAlertRuleCommand toCommandFromResource(
            CreateAlertRuleResource resource) {
        return new CreateAlertRuleCommand(
                resource.name(),
                resource.metric(),
                resource.operator(),
                resource.threshold(),
                resource.severity(),
                resource.description(),
                resource.active());
    }
}
