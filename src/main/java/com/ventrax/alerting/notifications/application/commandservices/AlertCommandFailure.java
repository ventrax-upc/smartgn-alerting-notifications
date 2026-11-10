package com.ventrax.alerting.notifications.application.commandservices;

public sealed interface AlertCommandFailure
        permits AlertCommandFailure.Duplicate,
        AlertCommandFailure.NotFound,
        AlertCommandFailure.InvalidState,
        AlertCommandFailure.Invalid {

    String messageKey();

    record Duplicate() implements AlertCommandFailure {
        @Override
        public String messageKey() {
            return "alert.error.duplicate";
        }
    }

    record NotFound(Long alertId) implements AlertCommandFailure {
        @Override
        public String messageKey() {
            return "alert.error.notFound";
        }
    }

    record InvalidState(String messageKey) implements AlertCommandFailure {
    }

    record Invalid(String messageKey) implements AlertCommandFailure {
    }
}
