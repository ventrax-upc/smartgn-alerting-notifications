package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.util.regex.Pattern;

public record EventId(String value) {

    private static final int MAX_LENGTH = 128;
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");

    public EventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("telemetry.error.eventId.notBlank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("telemetry.error.eventId.size");
        }
        if (!ALLOWED_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("telemetry.error.eventId.size");
        }
    }
}
