package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.notifications.mode", havingValue = "live")
public class TwilioNotificationAdapter implements NotificationAdapter {

    private final NotificationProperties properties;
    private final org.springframework.web.client.RestClient restClient;

    public TwilioNotificationAdapter(
            NotificationProperties properties, ProviderHttpClientFactory clientFactory) {
        this.properties = properties;
        this.restClient = clientFactory.create();
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NotificationDispatchResult send(NotificationMessage message) {
        var twilio = properties.twilio();
        if (isBlank(twilio.accountSid()) || isBlank(twilio.authToken()) || isBlank(twilio.from())) {
            return NotificationDispatchResult.permanentFailure(
                    "CONFIGURATION", "Twilio credentials are not configured");
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", message.recipient());
        form.add("From", twilio.from());
        form.add("Body", message.subject() + "\n\n" + message.body());
        var credentials = twilio.accountSid() + ":" + twilio.authToken();
        var authorization = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        try {
            var response = restClient.post()
                    .uri("https://api.twilio.com/2010-04-01/Accounts/{sid}/Messages.json",
                            twilio.accountSid())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            var providerMessageId = response == null ? null : (String) response.get("sid");
            return NotificationDispatchResult.accepted(
                    providerMessageId == null ? "twilio-unknown" : providerMessageId);
        } catch (Exception exception) {
            log.warn("Twilio delivery failed for notification {}: {}",
                    message.notificationId(), exception.getMessage());
            return ProviderErrorMapper.map(exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
