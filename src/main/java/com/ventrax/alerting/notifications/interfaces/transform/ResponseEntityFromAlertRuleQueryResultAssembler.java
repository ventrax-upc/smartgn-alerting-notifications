package com.ventrax.alerting.notifications.interfaces.transform;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.shared.interfaces.rest.ProblemDetailFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public final class ResponseEntityFromAlertRuleQueryResultAssembler {

    private ResponseEntityFromAlertRuleQueryResultAssembler() {
    }

    public static ResponseEntity<?> toResponseEntityFromAlertRule(AlertRule rule) {
        return ResponseEntity.ok(
                AlertRuleResourceFromEntityAssembler.toResourceFromEntity(rule));
    }

    public static ResponseEntity<?> toResponseEntityFromList(List<AlertRule> rules) {
        return ResponseEntity.ok(rules.stream()
                .map(AlertRuleResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    public static ResponseEntity<?> notFound(MessageSource messageSource, Object... args) {
        return ProblemDetailFactory.response(
                HttpStatus.NOT_FOUND, "alert.rule.error.notFound", messageSource, args);
    }
}
