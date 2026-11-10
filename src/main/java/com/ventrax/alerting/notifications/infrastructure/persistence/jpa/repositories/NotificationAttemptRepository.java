package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories;

import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationAttemptRepository
        extends JpaRepository<NotificationAttempt, Long> {

    List<NotificationAttempt> findAllByNotificationIdOrderByAttemptNumberAsc(Long notificationId);
}
