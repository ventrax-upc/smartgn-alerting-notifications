package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;

/**
 * Adapter pattern contract: provider-specific integration (SendGrid, Twilio, Infobip).
 */
public interface NotificationAdapter {

    boolean supports(NotificationChannel channel);

    NotificationDispatchResult send(NotificationMessage message);
}
