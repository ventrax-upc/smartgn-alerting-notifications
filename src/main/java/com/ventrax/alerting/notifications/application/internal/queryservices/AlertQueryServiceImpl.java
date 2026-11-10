package com.ventrax.alerting.notifications.application.internal.queryservices;

import com.ventrax.alerting.notifications.application.queryservices.AlertQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertsQuery;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.ventrax.alerting.shared.application.result.PagedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public PagedResult<Alert> handle(GetAllAlertsQuery query) {
        Specification<Alert> specification = (root, criteriaQuery, builder) -> builder.conjunction();
        if (query.accountId() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("accountId"), query.accountId()));
        }
        if (query.supplyPointId() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("supplyPointId"), query.supplyPointId()));
        }
        if (query.status() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("status"), query.status()));
        }
        if (query.severity() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.equal(root.get("severity"), query.severity()));
        }
        if (query.from() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.greaterThanOrEqualTo(root.get("measuredAt"), query.from()));
        }
        if (query.to() != null) {
            specification = specification.and((root, criteriaQuery, builder) ->
                    builder.lessThanOrEqualTo(root.get("measuredAt"), query.to()));
        }

        var pageable = PageRequest.of(query.page(), query.size(),
                Sort.by(Sort.Direction.DESC, "measuredAt"));
        var page = alertRepository.findAll(specification, pageable);
        return new PagedResult<>(page.getContent(), page.getTotalElements(),
                page.getNumber(), page.getSize());
    }

    @Override
    public Optional<Alert> handle(GetAlertByIdQuery query) {
        return alertRepository.findById(query.alertId());
    }
}
