package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.notifications.mode", havingValue = "live")
public class SendGridNotificationAdapter implements NotificationAdapter {

    private final NotificationProperties properties;
    private final org.springframework.web.client.RestClient restClient;

    public SendGridNotificationAdapter(
            NotificationProperties properties, ProviderHttpClientFactory clientFactory) {
        this.properties = properties;
        this.restClient = clientFactory.create();
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public NotificationDispatchResult send(NotificationMessage message) {
        if (isBlank(properties.sendGrid().apiKey()) || isBlank(properties.sendGrid().from())) {
            return NotificationDispatchResult.permanentFailure(
                    "CONFIGURATION", "SendGrid credentials are not configured");
        }
        var body = Map.of(
                "personalizations", List.of(Map.of(
                        "to", List.of(Map.of("email", message.recipient())))),
                "from", Map.of("email", properties.sendGrid().from()),
                "subject", message.subject(),
                "content", List.of(Map.of(
                        "type", "text/plain",
                        "value", message.body())));
        try {
            var response = restClient.post()
                    .uri("https://api.sendgrid.com/v3/mail/send")
                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + properties.sendGrid().apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            var providerMessageId = response.getHeaders().getFirst("X-Message-Id");
            return NotificationDispatchResult.accepted(
                    providerMessageId == null
                            ? "sendgrid-" + UUID.randomUUID()
                            : providerMessageId);
        } catch (Exception exception) {
            log.warn("SendGrid delivery failed for notification {}: {}",
                    message.notificationId(), exception.getMessage());
            return ProviderErrorMapper.map(exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
