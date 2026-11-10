package com.ventrax.alerting.notifications.infrastructure.notifications;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process circuit breaker per external provider.
 *
 * <p>It opens after five consecutive transient failures and stays open for 30 seconds.
 * The next call after that window is a single half-open probe; a failure reopens the
 * circuit, a success resets it.</p>
 */
@Component
public class ProviderCircuitBreaker {

    static final int FAILURE_THRESHOLD = 5;
    static final Duration OPEN_DURATION = Duration.ofSeconds(30);

    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final Clock clock;

    public ProviderCircuitBreaker(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(String provider) {
        var state = states.computeIfAbsent(provider, ignored -> new State());
        synchronized (state) {
            if (state.openUntil == null) {
                return true;
            }
            if (Instant.now(clock).isBefore(state.openUntil)) {
                return false;
            }
            state.openUntil = null;
            state.halfOpen = true;
            return true;
        }
    }

    public void recordSuccess(String provider) {
        var state = states.computeIfAbsent(provider, ignored -> new State());
        synchronized (state) {
            state.consecutiveFailures = 0;
            state.openUntil = null;
            state.halfOpen = false;
        }
    }

    public void recordFailure(String provider) {
        var state = states.computeIfAbsent(provider, ignored -> new State());
        synchronized (state) {
            state.consecutiveFailures++;
            if (state.halfOpen || state.consecutiveFailures >= FAILURE_THRESHOLD) {
                state.openUntil = Instant.now(clock).plus(OPEN_DURATION);
                state.halfOpen = false;
            }
        }
    }

    private static final class State {
        private int consecutiveFailures;
        private Instant openUntil;
        private boolean halfOpen;
    }
}
