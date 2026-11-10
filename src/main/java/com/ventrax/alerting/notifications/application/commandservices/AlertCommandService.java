package com.ventrax.alerting.notifications.application.commandservices;

import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.commands.AcknowledgeAlertCommand;
import com.ventrax.alerting.notifications.domain.model.commands.EvaluateTelemetryCommand;
import com.ventrax.alerting.notifications.domain.model.commands.ResolveAlertCommand;
import com.ventrax.alerting.shared.application.result.Result;

import java.util.List;

public interface AlertCommandService {

    Result<List<Alert>, AlertCommandFailure> handle(EvaluateTelemetryCommand command);

    Result<Alert, AlertCommandFailure> handle(AcknowledgeAlertCommand command);

    Result<Alert, AlertCommandFailure> handle(ResolveAlertCommand command);
}
