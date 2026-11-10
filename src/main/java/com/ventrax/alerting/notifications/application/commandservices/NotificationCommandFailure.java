package com.ventrax.alerting.notifications.application.commandservices;

public sealed interface NotificationCommandFailure
        permits NotificationCommandFailure.NotFound,
        NotificationCommandFailure.InvalidState,
        NotificationCommandFailure.NoRecipient,
        NotificationCommandFailure.Duplicate,
        NotificationCommandFailure.Invalid {

    String messageKey();

    record NotFound(Long notificationId) implements NotificationCommandFailure {
        @Override
        public String messageKey() {
            return "notification.error.notFound";
        }
    }

    record InvalidState(String messageKey) implements NotificationCommandFailure {
    }

    record NoRecipient() implements NotificationCommandFailure {
        @Override
        public String messageKey() {
            return "notification.error.noRecipient";
        }
    }

    record Duplicate() implements NotificationCommandFailure {
        @Override
        public String messageKey() {
            return "notification.error.duplicate";
        }
    }

    record Invalid(String messageKey) implements NotificationCommandFailure {
    }
}
