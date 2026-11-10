package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationStrategy;
import com.ventrax.alerting.notifications.application.outbound.NotificationStrategyResolver;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationStrategyRegistry implements NotificationStrategyResolver {

    private final Map<NotificationChannel, NotificationStrategy> strategies =
            new EnumMap<>(NotificationChannel.class);

    public NotificationStrategyRegistry(List<NotificationStrategy> discovered) {
        for (var strategy : discovered) {
            strategies.put(strategy.channel(), strategy);
        }
    }

    @Override
    public NotificationStrategy resolve(NotificationChannel channel) {
        var strategy = strategies.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("notification.preference.error.channel.invalid");
        }
        return strategy;
    }
}
