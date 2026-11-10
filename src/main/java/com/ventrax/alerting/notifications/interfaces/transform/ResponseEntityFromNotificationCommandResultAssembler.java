package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandFailure;
import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.shared.application.result.Result;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public final class ResponseEntityFromNotificationCommandResultAssembler {

    private ResponseEntityFromNotificationCommandResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromOperationalResult(
            Result<List<Notification>, NotificationCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                notifications -> ResponseEntity.status(CREATED).body(notifications.stream()
                        .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()),
                failure -> failureResponse(failure, messageSource));
    }

    public static ResponseEntity<?> toResponseEntityFromNotificationResult(
            Result<Notification, NotificationCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                notification -> ResponseEntity.ok(
                        NotificationResourceFromEntityAssembler.toResourceFromEntity(
                                notification)),
                failure -> failureResponse(failure, messageSource));
    }

    private static ResponseEntity<?> failureResponse(
            NotificationCommandFailure failure, MessageSource messageSource) {
        HttpStatus status = switch (failure) {
            case NotificationCommandFailure.NotFound ignored -> NOT_FOUND;
            case NotificationCommandFailure.InvalidState ignored -> CONFLICT;
            case NotificationCommandFailure.NoRecipient ignored -> UNPROCESSABLE_ENTITY;
            case NotificationCommandFailure.Duplicate ignored -> CONFLICT;
            case NotificationCommandFailure.Invalid ignored -> HttpStatus.BAD_REQUEST;
        };
        return ProblemDetailFactory.response(
                status, failure.messageKey(), messageSource);
    }
}
