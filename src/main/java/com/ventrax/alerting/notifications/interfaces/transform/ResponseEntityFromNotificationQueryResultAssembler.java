package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public final class ResponseEntityFromNotificationQueryResultAssembler {

    private ResponseEntityFromNotificationQueryResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromNotification(Notification notification) {
        return ResponseEntity.ok(
                NotificationResourceFromEntityAssembler.toResourceFromEntity(notification));
    }

    public static ResponseEntity<?> toResponseEntityFromList(List<Notification> notifications) {
        return ResponseEntity.ok(notifications.stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    public static ResponseEntity<?> toResponseEntityFromAttemptList(
            List<NotificationAttempt> attempts) {
        return ResponseEntity.ok(attempts.stream()
                .map(NotificationAttemptResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    public static ResponseEntity<?> notFound(MessageSource messageSource) {
        return ProblemDetailFactory.response(
                HttpStatus.NOT_FOUND, "notification.error.notFound", messageSource);
    }

    public static ResponseEntity<?> forbidden(MessageSource messageSource) {
        return ProblemDetailFactory.response(
                HttpStatus.FORBIDDEN, "errors.forbidden", messageSource);
    }
}
