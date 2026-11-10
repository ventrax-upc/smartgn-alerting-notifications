package com.ventrax.alerting.notifications.infrastructure.scheduling;

import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandService;
import com.ventrax.alerting.notifications.application.queryservices.NotificationQueryService;
import com.ventrax.alerting.notifications.domain.model.commands.DispatchNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.queries.GetPendingNotificationsQuery;
import com.ventrax.alerting.notifications.infrastructure.notifications.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.notifications.dispatch-enabled",
        havingValue = "true", matchIfMissing = true)
public class NotificationDispatchScheduler {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationProperties notificationProperties;
    private final DistributedDispatchLock dispatchLock;

    public NotificationDispatchScheduler(
            NotificationQueryService notificationQueryService,
            NotificationCommandService notificationCommandService,
            NotificationProperties notificationProperties,
            DistributedDispatchLock dispatchLock) {
        this.notificationQueryService = notificationQueryService;
        this.notificationCommandService = notificationCommandService;
        this.notificationProperties = notificationProperties;
        this.dispatchLock = dispatchLock;
    }

    @Scheduled(fixedDelayString = "${smartgn.notifications.dispatch-interval-ms:1000}")
    public void dispatchPending() {
        var handle = dispatchLock.tryAcquire();
        if (handle.isEmpty()) {
            return;
        }
        try (var ignored = handle.get()) {
            var pending = notificationQueryService.handle(
                    new GetPendingNotificationsQuery(notificationProperties.safeBatchSize()));
            for (var notification : pending) {
                try {
                    notificationCommandService.handle(
                            new DispatchNotificationCommand(notification.getId()));
                } catch (Exception exception) {
                    log.error("Unexpected error dispatching notification {}: {}",
                            notification.getId(), exception.getMessage(), exception);
                }
            }
        }
    }
}
