package com.ventrax.alerting.notifications.application.outbound;

import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;

/**
 * Strategy pattern contract: one implementation per notification channel.
 */
public interface NotificationStrategy {

    NotificationChannel channel();

    NotificationDispatchResult send(NotificationMessage message);
}
