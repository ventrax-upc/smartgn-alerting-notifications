package com.ventrax.alerting.notifications.infrastructure.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartgn.notifications")
public record NotificationProperties(
        String mode,
        boolean dispatchEnabled,
        long dispatchIntervalMs,
        int batchSize,
        int timeoutSeconds,
        SendGrid sendGrid,
        Twilio twilio,
        Infobip infobip) {

    public boolean simulated() {
        return mode == null || !"live".equalsIgnoreCase(mode);
    }

    public int safeBatchSize() {
        return Math.max(1, Math.min(500, batchSize));
    }

    public record SendGrid(String apiKey, String from) {
    }

    public record Twilio(String accountSid, String authToken, String from) {
    }

    public record Infobip(String baseUrl, String apiKey, String whatsappFrom) {
    }
}
