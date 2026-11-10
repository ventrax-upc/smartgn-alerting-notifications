package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Deterministic adapter used for local development and automated tests.
 * It never performs network calls.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.notifications.mode", havingValue = "simulated",
        matchIfMissing = true)
public class SimulatedNotificationAdapter implements NotificationAdapter {

    @Override
    public boolean supports(NotificationChannel channel) {
        return true;
    }

    @Override
    public NotificationDispatchResult send(NotificationMessage message) {
        var recipient = message.recipient() == null ? "" : message.recipient().toLowerCase();
        if (recipient.contains("indeterminate")) {
            log.info("Simulated indeterminate result for notification {}", message.notificationId());
            return NotificationDispatchResult.indeterminate(
                    "SIMULATED_INDETERMINATE", "Simulated indeterminate provider result");
        }
        if (recipient.contains("retry")) {
            log.info("Simulated transient failure for notification {}", message.notificationId());
            return NotificationDispatchResult.transientFailure(
                    "SIMULATED_TRANSIENT", "Simulated transient provider failure");
        }
        if (recipient.contains("fail")) {
            log.info("Simulated permanent failure for notification {}", message.notificationId());
            return NotificationDispatchResult.permanentFailure(
                    "SIMULATED_PERMANENT", "Simulated permanent provider failure");
        }
        var providerMessageId = "sim-" + UUID.randomUUID();
        log.info("Simulated {} notification {} accepted with id {}",
                message.channel(), message.notificationId(), providerMessageId);
        return NotificationDispatchResult.accepted(providerMessageId);
    }
}
