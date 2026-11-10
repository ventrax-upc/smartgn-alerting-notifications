package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.commands.UpdateAlertRuleCommand;
import com.ventrax.alerting.notifications.interfaces.resources.UpdateAlertRuleResource;

public final class UpdateAlertRuleCommandFromResourceAssembler {

    private UpdateAlertRuleCommandFromResourceAssembler() {
    }

    public static UpdateAlertRuleCommand toCommandFromResource(
            Long ruleId, UpdateAlertRuleResource resource) {
        return new UpdateAlertRuleCommand(
                ruleId,
                resource.name(),
                resource.metric(),
                resource.operator(),
                resource.threshold(),
                resource.severity(),
                resource.description(),
                resource.active());
    }
}
