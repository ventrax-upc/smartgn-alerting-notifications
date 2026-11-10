package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.application.commandservices.NotificationPreferenceCommandFailure;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.shared.application.result.Result;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

public final class ResponseEntityFromNotificationPreferenceCommandResultAssembler {

    private ResponseEntityFromNotificationPreferenceCommandResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<NotificationPreference, NotificationPreferenceCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                preference -> ResponseEntity.ok(
                        NotificationPreferenceResourceFromEntityAssembler
                                .toResourceFromEntity(preference)),
                failure -> ProblemDetailFactory.response(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        failure.messageKey(),
                        messageSource));
    }
}
