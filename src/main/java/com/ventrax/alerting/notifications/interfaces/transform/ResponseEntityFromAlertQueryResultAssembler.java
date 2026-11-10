package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public final class ResponseEntityFromAlertQueryResultAssembler {

    private ResponseEntityFromAlertQueryResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromAlert(Alert alert) {
        return ResponseEntity.ok(AlertResourceFromEntityAssembler.toResourceFromEntity(alert));
    }

    public static ResponseEntity<?> toResponseEntityFromList(List<Alert> alerts) {
        return ResponseEntity.ok(alerts.stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    public static ResponseEntity<?> notFound(MessageSource messageSource, Object... args) {
        return ProblemDetailFactory.response(
                HttpStatus.NOT_FOUND, "alert.error.notFound", messageSource, args);
    }

    public static ResponseEntity<?> forbidden(MessageSource messageSource) {
        return ProblemDetailFactory.response(
                HttpStatus.FORBIDDEN, "errors.forbidden", messageSource);
    }
}
