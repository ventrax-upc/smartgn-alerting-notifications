package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories;

import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    List<Notification> findAllByAlertIdOrderByCreatedAtAsc(Long alertId);

    boolean existsByAlertIdAndChannelAndRecipient(
            Long alertId, NotificationChannel channel, Recipient recipient);

    boolean existsBySourceEventIdAndChannelAndRecipient(
            EventId sourceEventId, NotificationChannel channel, Recipient recipient);

    Optional<Notification> findByProviderMessageId(String providerMessageId);

    @Query(value = """
            select * from notifications
            where status = 'PENDING' and next_attempt_at <= now()
            order by created_at asc
            limit :limit
            """, nativeQuery = true)
    List<Notification> findPending(@Param("limit") int limit);
}
