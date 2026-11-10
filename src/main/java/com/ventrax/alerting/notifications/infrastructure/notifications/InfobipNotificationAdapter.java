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

@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.notifications.mode", havingValue = "live")
public class InfobipNotificationAdapter implements NotificationAdapter {

    private final NotificationProperties properties;
    private final org.springframework.web.client.RestClient restClient;

    public InfobipNotificationAdapter(
            NotificationProperties properties, ProviderHttpClientFactory clientFactory) {
        this.properties = properties;
        this.restClient = clientFactory.create();
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.WHATSAPP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NotificationDispatchResult send(NotificationMessage message) {
        var infobip = properties.infobip();
        if (isBlank(infobip.baseUrl()) || isBlank(infobip.apiKey())
                || isBlank(infobip.whatsappFrom())) {
            return NotificationDispatchResult.permanentFailure(
                    "CONFIGURATION", "Infobip credentials are not configured");
        }
        var baseUrl = infobip.baseUrl().replaceAll("/+$", "");
        var body = Map.of(
                "from", infobip.whatsappFrom(),
                "to", message.recipient(),
                "content", Map.of("text", message.subject() + "\n\n" + message.body()));
        try {
            var response = restClient.post()
                    .uri(baseUrl + "/whatsapp/1/message/text")
                    .header(HttpHeaders.AUTHORIZATION, "App " + infobip.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            var providerMessageId = "infobip-unknown";
            if (response != null && response.get("messages") instanceof List<?> messages
                    && !messages.isEmpty() && messages.getFirst() instanceof Map<?, ?> first
                    && first.get("messageId") != null) {
                providerMessageId = first.get("messageId").toString();
            }
            return NotificationDispatchResult.accepted(providerMessageId);
        } catch (Exception exception) {
            log.warn("Infobip delivery failed for notification {}: {}",
                    message.notificationId(), exception.getMessage());
            return ProviderErrorMapper.map(exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
