package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.application.outbound.NotificationStrategy;

public abstract class AbstractNotificationStrategy implements NotificationStrategy {

    private final String provider;
    private final NotificationAdapterRegistry adapterRegistry;
    private final ProviderCircuitBreaker circuitBreaker;

    protected AbstractNotificationStrategy(
            String provider,
            NotificationAdapterRegistry adapterRegistry,
            ProviderCircuitBreaker circuitBreaker) {
        this.provider = provider;
        this.adapterRegistry = adapterRegistry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public NotificationDispatchResult send(NotificationMessage message) {
        if (!circuitBreaker.allow(provider)) {
            return NotificationDispatchResult.circuitOpen();
        }
        var result = adapterRegistry.adapterFor(channel()).send(message);
        switch (result.outcome()) {
            case ACCEPTED, DELIVERED -> circuitBreaker.recordSuccess(provider);
            case TRANSIENT_FAILURE, INDETERMINATE -> circuitBreaker.recordFailure(provider);
            case PERMANENT_FAILURE, CIRCUIT_OPEN -> {
                // A permanent failure is request-specific; it must not open the provider circuit.
            }
        }
        return result;
    }
}
