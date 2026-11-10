package com.ventrax.alerting.notifications.application.outbound;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;

public interface NotificationStrategyResolver {

    NotificationStrategy resolve(NotificationChannel channel);
}
