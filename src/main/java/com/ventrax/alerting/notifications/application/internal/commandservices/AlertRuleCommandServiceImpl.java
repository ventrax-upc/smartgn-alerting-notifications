package com.ventrax.alerting.notifications.application.internal.commandservices;

import com.ventrax.alerting.notifications.application.commandservices.AlertRuleCommandFailure;
import com.ventrax.alerting.notifications.application.commandservices.AlertRuleCommandService;
import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateAlertRuleCommand;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import com.ventrax.alerting.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AlertRuleCommandServiceImpl implements AlertRuleCommandService {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleCommandServiceImpl(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public Result<AlertRule, AlertRuleCommandFailure> handle(CreateAlertRuleCommand command) {
        try {
            if (alertRuleRepository.existsByName(command.name())) {
                return Result.failure(new AlertRuleCommandFailure.Duplicate());
            }
            var rule = alertRuleRepository.save(new AlertRule(command));
            log.info("Alert rule {} created with id {}", rule.getName(), rule.getId());
            return Result.success(rule);
        } catch (IllegalArgumentException exception) {
            return Result.failure(new AlertRuleCommandFailure.Invalid(exception.getMessage()));
        }
    }

    @Override
    public Result<AlertRule, AlertRuleCommandFailure> handle(UpdateAlertRuleCommand command) {
        var rule = alertRuleRepository.findById(command.ruleId()).orElse(null);
        if (rule == null) {
            return Result.failure(new AlertRuleCommandFailure.NotFound(command.ruleId()));
        }
        if (alertRuleRepository.existsByNameAndIdNot(command.name(), command.ruleId())) {
            return Result.failure(new AlertRuleCommandFailure.Duplicate());
        }
        try {
            rule.update(command);
            var saved = alertRuleRepository.save(rule);
            log.info("Alert rule {} updated to version {}", saved.getName(), saved.getVersion());
            return Result.success(saved);
        } catch (IllegalArgumentException exception) {
            return Result.failure(new AlertRuleCommandFailure.Invalid(exception.getMessage()));
        }
    }
}
