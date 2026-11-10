package com.ventrax.alerting.notifications.domain.model.services;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationRetryPolicyTest {

    @Test
    void appliesBackoffThenRecheckInterval() {
        assertEquals(Duration.ofSeconds(1), NotificationRetryPolicy.nextDelay(1));
        assertEquals(Duration.ofSeconds(2), NotificationRetryPolicy.nextDelay(2));
        assertEquals(Duration.ofSeconds(4), NotificationRetryPolicy.nextDelay(3));
        assertEquals(Duration.ofSeconds(30), NotificationRetryPolicy.nextDelay(4));
        assertEquals(Duration.ofSeconds(30), NotificationRetryPolicy.nextDelay(20));
    }
}
