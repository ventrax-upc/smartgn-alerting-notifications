package com.ventrax.alerting.notifications.application.internal.seed;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "smartgn.alerting.seed-default-rules",
        havingValue = "true", matchIfMissing = true)
public class DefaultAlertRulesSeeder implements ApplicationRunner {

    private final AlertRuleRepository alertRuleRepository;

    public DefaultAlertRulesSeeder(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        defaultRules().forEach(command -> {
            if (alertRuleRepository.existsByName(command.name())) {
                return;
            }
            try {
                var rule = alertRuleRepository.save(new AlertRule(command));
                log.info("Default alert rule {} created with id {}", rule.getName(), rule.getId());
            } catch (RuntimeException exception) {
                log.warn("Default alert rule {} could not be created: {}",
                        command.name(), exception.getMessage());
            }
        });
    }

    private List<CreateAlertRuleCommand> defaultRules() {
        return List.of(
                new CreateAlertRuleCommand(
                        "Presencia de gas detectada",
                        RuleMetric.GAS_PRESENCE,
                        RuleOperator.IS_TRUE,
                        null,
                        AlertSeverity.CRITICAL,
                        "Detecta presencia de gas reportada por el sensor.",
                        true),
                new CreateAlertRuleCommand(
                        "Presión crítica baja",
                        RuleMetric.PRESSURE,
                        RuleOperator.LESS_THAN,
                        new BigDecimal("0.5000"),
                        AlertSeverity.CRITICAL,
                        "Presión por debajo del umbral crítico de seguridad.",
                        true),
                new CreateAlertRuleCommand(
                        "Sobrepresión crítica",
                        RuleMetric.PRESSURE,
                        RuleOperator.GREATER_THAN,
                        new BigDecimal("5.0000"),
                        AlertSeverity.CRITICAL,
                        "Presión por encima del umbral crítico de seguridad.",
                        true),
                new CreateAlertRuleCommand(
                        "Consumo anómalo",
                        RuleMetric.CONSUMPTION,
                        RuleOperator.GREATER_THAN,
                        new BigDecimal("100.0000"),
                        AlertSeverity.WARNING,
                        "Consumo por encima del umbral configurado.",
                        true),
                new CreateAlertRuleCommand(
                        "Válvula abierta observada",
                        RuleMetric.VALVE_STATE,
                        RuleOperator.IS_TRUE,
                        null,
                        AlertSeverity.WARNING,
                        "La válvula reporta estado abierto; por sí sola no confirma una fuga.",
                        true)
        );
    }
}
