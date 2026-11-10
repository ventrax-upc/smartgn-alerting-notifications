package com.ventrax.alerting.notifications.domain.model.valueobjects;

import java.util.regex.Pattern;

public record Recipient(String value) {

    private static final int MAX_LENGTH = 320;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{6,14}$");

    public Recipient {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("notification.preference.error.recipient.notBlank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("notification.preference.error.recipient.size");
        }
    }

    public void validateFor(NotificationChannel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("notification.preference.error.channel.invalid");
        }
        boolean valid = switch (channel) {
            case EMAIL -> EMAIL_PATTERN.matcher(value).matches();
            case SMS, WHATSAPP -> PHONE_PATTERN.matcher(value).matches();
        };
        if (!valid) {
            throw new IllegalArgumentException("notification.preference.error.recipient.invalid");
        }
    }
}
