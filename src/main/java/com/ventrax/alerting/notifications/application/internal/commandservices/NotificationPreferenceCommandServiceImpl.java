package com.ventrax.alerting.notifications.application.internal.commandservices;

import com.ventrax.alerting.notifications.application.commandservices.NotificationPreferenceCommandFailure;
import com.ventrax.alerting.notifications.application.commandservices.NotificationPreferenceCommandService;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.ventrax.alerting.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class NotificationPreferenceCommandServiceImpl
        implements NotificationPreferenceCommandService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceCommandServiceImpl(
            NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    public Result<NotificationPreference, NotificationPreferenceCommandFailure> handle(
            UpdateNotificationPreferenceCommand command) {
        try {
            var existing = notificationPreferenceRepository
                    .findByAccountIdAndNotificationType(
                            command.accountId(), command.notificationType());
            NotificationPreference preference;
            if (existing.isPresent()) {
                preference = existing.get();
                preference.update(command);
            } else {
                preference = new NotificationPreference(command);
            }
            var saved = notificationPreferenceRepository.save(preference);
            log.info("Notification preference for account {} and type {} saved with channel {}",
                    command.accountId().value(), command.notificationType(), saved.getChannel());
            return Result.success(saved);
        } catch (IllegalArgumentException exception) {
            return Result.failure(new NotificationPreferenceCommandFailure.Invalid(
                    exception.getMessage()));
        }
    }
}
