package com.ventrax.alerting.notifications.application.commandservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateAlertRuleCommand;
import com.ventrax.alerting.shared.application.result.Result;

public interface AlertRuleCommandService {

    Result<AlertRule, AlertRuleCommandFailure> handle(CreateAlertRuleCommand command);

    Result<AlertRule, AlertRuleCommandFailure> handle(UpdateAlertRuleCommand command);
}
