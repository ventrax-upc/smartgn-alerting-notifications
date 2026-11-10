package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.application.commandservices.AlertRuleCommandFailure;
import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.shared.application.result.Result;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public final class ResponseEntityFromAlertRuleCommandResultAssembler {

    private ResponseEntityFromAlertRuleCommandResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromCreateResult(
            Result<AlertRule, AlertRuleCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                rule -> ResponseEntity.status(CREATED)
                        .body(AlertRuleResourceFromEntityAssembler.toResourceFromEntity(rule)),
                failure -> failureResponse(failure, messageSource));
    }

    public static ResponseEntity<?> toResponseEntityFromUpdateResult(
            Result<AlertRule, AlertRuleCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                rule -> ResponseEntity.ok(
                        AlertRuleResourceFromEntityAssembler.toResourceFromEntity(rule)),
                failure -> failureResponse(failure, messageSource));
    }

    private static ResponseEntity<?> failureResponse(
            AlertRuleCommandFailure failure, MessageSource messageSource) {
        HttpStatus status = switch (failure) {
            case AlertRuleCommandFailure.Duplicate ignored -> CONFLICT;
            case AlertRuleCommandFailure.NotFound ignored -> NOT_FOUND;
            case AlertRuleCommandFailure.Invalid ignored -> HttpStatus.BAD_REQUEST;
        };
        Object[] args = failure instanceof AlertRuleCommandFailure.NotFound notFound
                ? new Object[]{notFound.ruleId()}
                : new Object[0];
        return ProblemDetailFactory.response(
                status, failure.messageKey(), messageSource, args);
    }
}
