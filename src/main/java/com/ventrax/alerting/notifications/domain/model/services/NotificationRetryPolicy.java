package com.ventrax.alerting.notifications.domain.model.services;

import java.time.Duration;

/**
 * Retry policy for the notification outbox.
 *
 * <p>Each delivery cycle starts with one attempt and up to three retries with
 * 1, 2 and 4 second waits. If the transient failure persists, the request stays
 * PENDING and is checked again after 30 seconds, respecting the circuit breaker.</p>
 */
public final class NotificationRetryPolicy {

    public static final int MAX_INITIAL_RETRIES = 3;
    public static final Duration RECHECK_INTERVAL = Duration.ofSeconds(30);

    private static final Duration[] BACKOFF = {
            Duration.ofSeconds(1),
            Duration.ofSeconds(2),
            Duration.ofSeconds(4)
    };

    private NotificationRetryPolicy() {
    }

    public static Duration nextDelay(int failedAttempts) {
        if (failedAttempts <= 0) return BACKOFF[0];
        if (failedAttempts <= MAX_INITIAL_RETRIES) return BACKOFF[failedAttempts - 1];
        return RECHECK_INTERVAL;
    }
}
