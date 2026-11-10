package com.ventrax.alerting.notifications.application.internal.queryservices;

import com.ventrax.alerting.notifications.application.queryservices.NotificationPreferenceQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationPreferencesByAccountQuery;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class NotificationPreferenceQueryServiceImpl implements NotificationPreferenceQueryService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceQueryServiceImpl(
            NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    public List<NotificationPreference> handle(GetNotificationPreferencesByAccountQuery query) {
        return notificationPreferenceRepository
                .findAllByAccountIdOrderByNotificationTypeAsc(query.accountId());
    }
}
