package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppNotificationStrategy extends AbstractNotificationStrategy {

    public WhatsAppNotificationStrategy(
            NotificationAdapterRegistry adapterRegistry,
            ProviderCircuitBreaker circuitBreaker) {
        super("INFOBIP", adapterRegistry, circuitBreaker);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }
}
