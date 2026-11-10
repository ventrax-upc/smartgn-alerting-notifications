package com.ventrax.alerting.notifications.application.commandservices;

public sealed interface NotificationPreferenceCommandFailure
        permits NotificationPreferenceCommandFailure.Invalid {

    String messageKey();

    record Invalid(String messageKey) implements NotificationPreferenceCommandFailure {
    }
}
