package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories;

import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByAccountIdAndNotificationType(
            AccountId accountId, NotificationType notificationType);

    List<NotificationPreference> findAllByAccountIdOrderByNotificationTypeAsc(AccountId accountId);
}
