package com.ventrax.alerting.notifications.application.internal.queryservices;

import com.ventrax.alerting.notifications.application.queryservices.NotificationQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.entities.NotificationAttempt;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationAttemptsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationByProviderMessageIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationsByAlertQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetPendingNotificationsQuery;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationAttemptRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import com.ventrax.alerting.shared.application.result.PagedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationAttemptRepository notificationAttemptRepository;

    public NotificationQueryServiceImpl(
            NotificationRepository notificationRepository,
            NotificationAttemptRepository notificationAttemptRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationAttemptRepository = notificationAttemptRepository;
    }

    @Override
    public PagedResult<Notification> handle(GetAllNotificationsQuery query) {
        Specification<Notification> specification =
                (root, criteriaQuery, builder) -> builder.conjunction();
        if (query.accountId() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("accountId"), query.accountId()));
        }
        if (query.alertId() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("alertId"), query.alertId()));
        }
        if (query.status() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("status"), query.status()));
        }
        var pageable = PageRequest.of(query.page(), query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        var page = notificationRepository.findAll(specification, pageable);
        return new PagedResult<>(page.getContent(), page.getTotalElements(),
                page.getNumber(), page.getSize());
    }

    @Override
    public List<Notification> handle(GetNotificationsByAlertQuery query) {
        return notificationRepository.findAllByAlertIdOrderByCreatedAtAsc(query.alertId());
    }

    @Override
    public List<Notification> handle(GetPendingNotificationsQuery query) {
        return notificationRepository.findPending(query.limit());
    }

    @Override
    public Optional<Notification> handle(GetNotificationByIdQuery query) {
        return notificationRepository.findById(query.notificationId());
    }

    @Override
    public Optional<Notification> handle(GetNotificationByProviderMessageIdQuery query) {
        return notificationRepository.findByProviderMessageId(query.providerMessageId());
    }

    @Override
    public List<NotificationAttempt> handle(GetNotificationAttemptsQuery query) {
        return notificationAttemptRepository
                .findAllByNotificationIdOrderByAttemptNumberAsc(query.notificationId());
    }
}
