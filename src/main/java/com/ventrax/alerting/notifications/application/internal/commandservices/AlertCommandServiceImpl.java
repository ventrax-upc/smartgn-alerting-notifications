package com.ventrax.alerting.notifications.application.internal.commandservices;

import com.ventrax.alerting.notifications.application.commandservices.AlertCommandFailure;
import com.ventrax.alerting.notifications.application.commandservices.AlertCommandService;
import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.commands.AcknowledgeAlertCommand;
import com.ventrax.alerting.notifications.domain.model.commands.EvaluateTelemetryCommand;
import com.ventrax.alerting.notifications.domain.model.commands.ResolveAlertCommand;
import com.ventrax.alerting.notifications.domain.model.services.AlertGuidanceCatalog;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import com.ventrax.alerting.notifications.domain.model.valueobjects.Recipient;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import com.ventrax.alerting.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public AlertCommandServiceImpl(
            AlertRepository alertRepository,
            AlertRuleRepository alertRuleRepository,
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    public Result<List<Alert>, AlertCommandFailure> handle(EvaluateTelemetryCommand command) {
        var reading = command.reading();
        if (!reading.hasAnyMetric()) {
            return Result.failure(new AlertCommandFailure.Invalid("alert.error.metric.missing"));
        }
        if (command.measuredAt().isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            return Result.failure(new AlertCommandFailure.Invalid("alert.error.measuredAt.invalid"));
        }

        var rules = alertRuleRepository.findAllByActiveTrueOrderByNameAsc();
        var created = new ArrayList<Alert>();
        try {
            for (var rule : rules) {
                if (!rule.matches(reading)) {
                    continue;
                }
                if (alertRepository.existsByDeviceIdAndEventIdAndRuleId(
                        command.deviceId(), command.eventId(), rule.getId())) {
                    log.debug("Duplicate alert ignored for event {} and rule {}",
                            command.eventId().value(), rule.getId());
                    continue;
                }
                var measured = reading.valueFor(rule.getMetric());
                var condition = rule.describeCondition(measured);
                var instructions = AlertGuidanceCatalog.instructionsFor(
                        rule.getMetric(), rule.getSeverity());
                var alert = alertRepository.saveAndFlush(
                        new Alert(command, rule, measured, condition, instructions));
                createNotificationIfPossible(alert, command);
                created.add(alert);
            }
            log.info("Telemetry event {} evaluated against {} rules, {} alerts created",
                    command.eventId().value(), rules.size(), created.size());
            return Result.success(created);
        } catch (IllegalArgumentException exception) {
            return Result.failure(new AlertCommandFailure.Invalid(exception.getMessage()));
        }
    }

    @Override
    public Result<Alert, AlertCommandFailure> handle(AcknowledgeAlertCommand command) {
        var alert = alertRepository.findById(command.alertId()).orElse(null);
        if (alert == null) {
            return Result.failure(new AlertCommandFailure.NotFound(command.alertId()));
        }
        try {
            alert.acknowledge();
            return Result.success(alertRepository.save(alert));
        } catch (IllegalStateException exception) {
            return Result.failure(new AlertCommandFailure.InvalidState(exception.getMessage()));
        }
    }

    @Override
    public Result<Alert, AlertCommandFailure> handle(ResolveAlertCommand command) {
        var alert = alertRepository.findById(command.alertId()).orElse(null);
        if (alert == null) {
            return Result.failure(new AlertCommandFailure.NotFound(command.alertId()));
        }
        try {
            alert.resolve();
            return Result.success(alertRepository.save(alert));
        } catch (IllegalStateException exception) {
            return Result.failure(new AlertCommandFailure.InvalidState(exception.getMessage()));
        }
    }

    private void createNotificationIfPossible(Alert alert, EvaluateTelemetryCommand command) {
        var preference = notificationPreferenceRepository
                .findByAccountIdAndNotificationType(
                        command.accountId(), NotificationType.CRITICAL_ALERT)
                .filter(NotificationPreference::isActive);

        NotificationChannel channel;
        Recipient recipient;
        if (preference.isPresent()) {
            channel = preference.get().getChannel();
            recipient = preference.get().getRecipient();
        } else if (command.recipientEmail() != null && !command.recipientEmail().isBlank()) {
            channel = NotificationChannel.EMAIL;
            recipient = new Recipient(command.recipientEmail());
        } else if (command.recipientPhone() != null && !command.recipientPhone().isBlank()) {
            channel = NotificationChannel.SMS;
            recipient = new Recipient(command.recipientPhone());
        } else {
            log.warn("Alert {} created without an active notification preference for account {}",
                    alert.getId(), command.accountId().value());
            return;
        }

        try {
            recipient.validateFor(channel);
            var subject = "[%s] %s - %s".formatted(
                    alert.getSeverity().displayName(),
                    alert.getRuleName(),
                    alert.getSupplyPointId().value());
            notificationRepository.save(new Notification(
                    alert, channel, recipient, subject, buildAlertBody(alert)));
        } catch (IllegalArgumentException exception) {
            log.warn("Alert {} created but notification recipient {} is invalid for {}: {}",
                    alert.getId(), recipient.value(), channel, exception.getMessage());
        }
    }

    private String buildAlertBody(Alert alert) {
        return """
                Alerta SmartGN
                Severidad: %s
                Regla: %s (versión %d)
                Inmueble: %s
                Punto de suministro: %s
                Dispositivo: %s
                Condición: %s
                Fecha de medición: %s
                Instrucciones:
                %s
                """.formatted(
                alert.getSeverity().displayName(),
                alert.getRuleName(),
                alert.getRuleVersion(),
                alert.getPropertyId().value(),
                alert.getSupplyPointId().value(),
                alert.getDeviceId().value(),
                alert.getCondition(),
                alert.getMeasuredAt(),
                alert.getInstructions());
    }
}
