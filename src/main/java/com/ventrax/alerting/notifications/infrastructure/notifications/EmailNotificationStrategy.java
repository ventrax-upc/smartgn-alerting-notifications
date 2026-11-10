package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationStrategy extends AbstractNotificationStrategy {

    public EmailNotificationStrategy(
            NotificationAdapterRegistry adapterRegistry,
            ProviderCircuitBreaker circuitBreaker) {
        super("SENDGRID", adapterRegistry, circuitBreaker);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }
}
