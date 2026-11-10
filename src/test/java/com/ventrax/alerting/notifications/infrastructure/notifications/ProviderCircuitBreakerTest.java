package com.ventrax.alerting.notifications.infrastructure.notifications;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderCircuitBreakerTest {

    @Test
    void opensAfterFiveFailuresAndAllowsHalfOpenProbe() {
        var clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        var breaker = new ProviderCircuitBreaker(clock);

        for (int i = 0; i < 5; i++) {
            breaker.recordFailure("SENDGRID");
        }
        assertFalse(breaker.allow("SENDGRID"));

        clock.advance(Duration.ofSeconds(31));
        assertTrue(breaker.allow("SENDGRID"));

        breaker.recordFailure("SENDGRID");
        assertFalse(breaker.allow("SENDGRID"));

        clock.advance(Duration.ofSeconds(31));
        assertTrue(breaker.allow("SENDGRID"));
        breaker.recordSuccess("SENDGRID");
        assertTrue(breaker.allow("SENDGRID"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
