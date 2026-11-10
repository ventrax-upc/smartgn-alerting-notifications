package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationAdapterRegistry {

    private final Map<NotificationChannel, NotificationAdapter> adapters =
            new EnumMap<>(NotificationChannel.class);

    public NotificationAdapterRegistry(List<NotificationAdapter> discovered) {
        for (var adapter : discovered) {
            for (var channel : NotificationChannel.values()) {
                if (adapter.supports(channel)) {
                    adapters.putIfAbsent(channel, adapter);
                }
            }
        }
    }

    public NotificationAdapter adapterFor(NotificationChannel channel) {
        var adapter = adapters.get(channel);
        if (adapter == null) {
            throw new IllegalArgumentException("notification.preference.error.channel.invalid");
        }
        return adapter;
    }
}
