package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationStrategy extends AbstractNotificationStrategy {

    public SmsNotificationStrategy(
            NotificationAdapterRegistry adapterRegistry,
            ProviderCircuitBreaker circuitBreaker) {
        super("TWILIO", adapterRegistry, circuitBreaker);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }
}
