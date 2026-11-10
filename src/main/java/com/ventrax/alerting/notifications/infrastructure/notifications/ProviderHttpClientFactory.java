package com.ventrax.alerting.notifications.infrastructure.notifications;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class ProviderHttpClientFactory {

    private final NotificationProperties properties;

    public ProviderHttpClientFactory(NotificationProperties properties) {
        this.properties = properties;
    }

    public RestClient create() {
        var factory = new SimpleClientHttpRequestFactory();
        var timeout = Duration.ofSeconds(Math.max(1, properties.timeoutSeconds()));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return RestClient.builder().requestFactory(factory).build();
    }
}
