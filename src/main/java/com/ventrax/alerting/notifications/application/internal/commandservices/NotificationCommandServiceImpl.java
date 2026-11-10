package com.ventrax.alerting.notifications.application.internal.commandservices;

import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandFailure;
import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandService;
import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import com.ventrax.alerting.notifications.application.outbound.NotificationMessage;
import com.ventrax.alerting.notifications.application.outbound.NotificationStrategyResolver;
import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.commands.CreateOperationalNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.DispatchNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.RecordNotificationReceiptCommand;
import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import com.ventrax.alerting.notifications.domain.model.services.NotificationRetryPolicy;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationAttemptRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import com.ventrax.alerting.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationAttemptRepository notificationAttemptRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationStrategyResolver notificationStrategyResolver;
    private final Clock clock;

    public NotificationCommandServiceImpl(
            NotificationRepository notificationRepository,
            NotificationAttemptRepository notificationAttemptRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            NotificationStrategyResolver notificationStrategyResolver,
            Clock clock) {
        this.notificationRepository = notificationRepository;
        this.notificationAttemptRepository = notificationAttemptRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationStrategyResolver = notificationStrategyResolver;
        this.clock = clock;
    }

    @Override
    public Result<List<Notification>, NotificationCommandFailure> handle(
            CreateOperationalNotificationCommand command) {
        var preference = notificationPreferenceRepository
                .findByAccountIdAndNotificationType(
                        command.accountId(), NotificationType.OPERATIONAL_NOTICE)
                .filter(NotificationPreference::isActive);
        if (preference.isEmpty()) {
            return Result.failure(new NotificationCommandFailure.NoRecipient());
        }
        var configured = preference.get();
        try {
            configured.getRecipient().validateFor(configured.getChannel());
            if (notificationRepository.existsBySourceEventIdAndChannelAndRecipient(
                    command.eventId(), configured.getChannel(), configured.getRecipient())) {
                return Result.failure(new NotificationCommandFailure.Duplicate());
            }
            var notification = notificationRepository.save(new Notification(
                    command.eventId(),
                    command.accountId(),
                    command.eventType(),
                    configured.getChannel(),
                    configured.getRecipient(),
                    command.subject(),
                    command.body()));
            log.info("Operational notification {} created for event {}",
                    notification.getId(), command.eventId().value());
            return Result.success(List.of(notification));
        } catch (IllegalArgumentException exception) {
            return Result.failure(new NotificationCommandFailure.Invalid(
                    exception.getMessage()));
        }
    }

    @Override
    public Result<Notification, NotificationCommandFailure> handle(
            DispatchNotificationCommand command) {
        var notification = notificationRepository.findById(command.notificationId()).orElse(null);
        if (notification == null) {
            return Result.failure(new NotificationCommandFailure.NotFound(command.notificationId()));
        }
        if (notification.getStatus() != DeliveryStatus.PENDING) {
            return Result.failure(new NotificationCommandFailure.InvalidState(
                    "notification.error.status.invalid"));
        }

        var message = new NotificationMessage(
                notification.getId(),
                notification.getAlertId(),
                notification.getNotificationType(),
                notification.getChannel(),
                notification.getRecipient().value(),
                notification.getSubject(),
                notification.getBody());

        var strategy = notificationStrategyResolver.resolve(notification.getChannel());
        var result = strategy.send(message);
        var now = Instant.now(clock);

        if (result.outcome() == NotificationDispatchResult.Outcome.CIRCUIT_OPEN) {
            notification.markCircuitOpen(now.plus(NotificationRetryPolicy.RECHECK_INTERVAL));
            notificationRepository.save(notification);
            log.debug("Circuit open; notification {} rescheduled", notification.getId());
            return Result.success(notification);
        }

        notification.markAttemptStarted();
        var attemptStatus = switch (result.outcome()) {
            case ACCEPTED -> DeliveryStatus.ACCEPTED;
            case DELIVERED -> DeliveryStatus.DELIVERED;
            case TRANSIENT_FAILURE, PERMANENT_FAILURE -> DeliveryStatus.FAILED;
            case INDETERMINATE -> DeliveryStatus.INDETERMINATE;
            case CIRCUIT_OPEN -> DeliveryStatus.PENDING;
        };
        notificationAttemptRepository.save(new NotificationAttempt(
                notification.getId(),
                notification.getAttempts(),
                attemptStatus,
                result.providerMessageId(),
                result.errorCode(),
                result.errorMessage()));

        switch (result.outcome()) {
            case ACCEPTED -> notification.recordAccepted(result.providerMessageId());
            case DELIVERED -> notification.recordDelivered();
            case TRANSIENT_FAILURE -> notification.recordTransientFailure(
                    result.errorMessage(),
                    now.plus(NotificationRetryPolicy.nextDelay(notification.getAttempts())));
            case PERMANENT_FAILURE -> notification.recordPermanentFailure(result.errorMessage());
            case INDETERMINATE -> notification.recordIndeterminate(result.errorMessage());
            case CIRCUIT_OPEN -> {
                // Handled above.
            }
        }
        var saved = notificationRepository.save(notification);
        log.info("Notification {} attempt {} finished with {}",
                saved.getId(), saved.getAttempts(), result.outcome());
        return Result.success(saved);
    }

    @Override
    public Result<Notification, NotificationCommandFailure> handle(
            RecordNotificationReceiptCommand command) {
        var notification = notificationRepository
                .findByProviderMessageId(command.providerMessageId())
                .orElse(null);
        if (notification == null) {
            return Result.failure(new NotificationCommandFailure.NotFound(null));
        }
        notification.markAttemptStarted();
        switch (command.status()) {
            case ACCEPTED -> notification.recordAccepted(command.providerMessageId());
            case DELIVERED -> notification.recordDelivered();
            case FAILED -> notification.recordPermanentFailure(command.errorMessage());
            case INDETERMINATE -> notification.recordIndeterminate(command.errorMessage());
            case PENDING -> {
                // Rejected by the command constructor.
            }
        }
        notificationAttemptRepository.save(new NotificationAttempt(
                notification.getId(),
                notification.getAttempts(),
                command.status(),
                command.providerMessageId(),
                command.errorCode(),
                command.errorMessage()));
        var saved = notificationRepository.save(notification);
        log.info("Notification {} receipt updated to {}", saved.getId(), saved.getStatus());
        return Result.success(saved);
    }
}
