package com.ventrax.alerting.notifications.application.internal.queryservices;

import com.ventrax.alerting.notifications.application.queryservices.AlertRuleQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertRuleByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertRulesQuery;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AlertRuleQueryServiceImpl implements AlertRuleQueryService {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleQueryServiceImpl(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public List<AlertRule> handle(GetAllAlertRulesQuery query) {
        if (Boolean.TRUE.equals(query.active())) {
            return alertRuleRepository.findAllByActiveTrueOrderByNameAsc();
        }
        if (Boolean.FALSE.equals(query.active())) {
            return alertRuleRepository.findAllByActiveFalseOrderByNameAsc();
        }
        return alertRuleRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Optional<AlertRule> handle(GetAlertRuleByIdQuery query) {
        return alertRuleRepository.findById(query.ruleId());
    }
}
