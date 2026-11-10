package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.application.commandservices.AlertCommandFailure;
import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.interfaces.resources.TelemetryEvaluationResource;
import com.ventrax.alerting.shared.application.result.Result;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public final class ResponseEntityFromAlertCommandResultAssembler {

    private ResponseEntityFromAlertCommandResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromEvaluationResult(
            Result<List<Alert>, AlertCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                alerts -> ResponseEntity.ok(new TelemetryEvaluationResource(
                        alerts.size(),
                        alerts.stream().map(Alert::getId).toList())),
                failure -> failureResponse(failure, messageSource));
    }

    public static ResponseEntity<?> toResponseEntityFromAlertResult(
            Result<Alert, AlertCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                alert -> ResponseEntity.ok(
                        AlertResourceFromEntityAssembler.toResourceFromEntity(alert)),
                failure -> failureResponse(failure, messageSource));
    }

    private static ResponseEntity<?> failureResponse(
            AlertCommandFailure failure, MessageSource messageSource) {
        HttpStatus status = switch (failure) {
            case AlertCommandFailure.Duplicate ignored -> CONFLICT;
            case AlertCommandFailure.NotFound ignored -> NOT_FOUND;
            case AlertCommandFailure.InvalidState ignored -> CONFLICT;
            case AlertCommandFailure.Invalid ignored -> HttpStatus.BAD_REQUEST;
        };
        Object[] args = failure instanceof AlertCommandFailure.NotFound notFound
                ? new Object[]{notFound.alertId()}
                : new Object[0];
        return ProblemDetailFactory.response(
                status, failure.messageKey(), messageSource, args);
    }
}
