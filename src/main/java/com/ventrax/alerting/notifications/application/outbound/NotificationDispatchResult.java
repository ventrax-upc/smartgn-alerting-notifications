package com.ventrax.alerting.notifications.application.outbound;

/**
 * Result of a single provider call.
 */
public record NotificationDispatchResult(
        Outcome outcome,
        String providerMessageId,
        String errorCode,
        String errorMessage) {

    public enum Outcome {
        ACCEPTED,
        DELIVERED,
        TRANSIENT_FAILURE,
        PERMANENT_FAILURE,
        INDETERMINATE,
        CIRCUIT_OPEN
    }

    public static NotificationDispatchResult accepted(String providerMessageId) {
        return new NotificationDispatchResult(Outcome.ACCEPTED, providerMessageId, null, null);
    }

    public static NotificationDispatchResult delivered(String providerMessageId) {
        return new NotificationDispatchResult(Outcome.DELIVERED, providerMessageId, null, null);
    }

    public static NotificationDispatchResult transientFailure(String code, String message) {
        return new NotificationDispatchResult(Outcome.TRANSIENT_FAILURE, null, code, message);
    }

    public static NotificationDispatchResult permanentFailure(String code, String message) {
        return new NotificationDispatchResult(Outcome.PERMANENT_FAILURE, null, code, message);
    }

    public static NotificationDispatchResult indeterminate(String code, String message) {
        return new NotificationDispatchResult(Outcome.INDETERMINATE, null, code, message);
    }

    public static NotificationDispatchResult circuitOpen() {
        return new NotificationDispatchResult(Outcome.CIRCUIT_OPEN, null, "CIRCUIT_OPEN",
                "The provider circuit breaker is open");
    }
}
