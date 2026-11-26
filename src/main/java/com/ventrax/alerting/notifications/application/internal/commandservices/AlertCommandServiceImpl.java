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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AlertCommandServiceImpl implements AlertCommandService {

    private final JdbcTemplate jdbc;
    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public AlertCommandServiceImpl(
            JdbcTemplate jdbc,
            AlertRepository alertRepository,
            AlertRuleRepository alertRuleRepository,
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository) {
        this.jdbc = jdbc;
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    public Result<List<Alert>, AlertCommandFailure> handle(EvaluateTelemetryCommand command) {
        var reading = command.reading();
        if (command.measuredAt().isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            return Result.failure(new AlertCommandFailure.Invalid("alert.error.measuredAt.invalid"));
        }

        var device = command.deviceId().value();
        var event = command.eventId().value();
        if (jdbc.update("""
                INSERT INTO alerting.telemetry_receipt (device_id, event_id) VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, device, event) == 0) {
            return Result.success(List.of());
        }

        var measuredAt = command.measuredAt().truncatedTo(ChronoUnit.MICROS).atOffset(ZoneOffset.UTC);
        jdbc.update("""
                INSERT INTO alerting.telemetry_watermark (device_id, measured_at) VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, device, measuredAt);
        var latest = jdbc.queryForObject("""
                SELECT measured_at FROM alerting.telemetry_watermark WHERE device_id = ? FOR UPDATE
                """, (row, index) -> row.getObject(1, OffsetDateTime.class), device);
        if (measuredAt.isAfter(latest)) {
            jdbc.update("UPDATE alerting.telemetry_watermark SET measured_at = ? WHERE device_id = ?",
                    measuredAt, device);
        }
        var historical = measuredAt.isBefore(latest)
                || command.measuredAt().isBefore(Instant.now().minusSeconds(30));

        var rules = alertRuleRepository.findAllByActiveTrueOrderByNameAsc();
        var created = new ArrayList<Alert>();
        for (var rule : rules) {
            if (!rule.matches(reading) || alertRepository.existsByDeviceIdAndEventIdAndRuleId(
                    command.deviceId(), command.eventId(), rule.getId())) {
                continue;
            }

            var measured = reading.valueFor(rule.getMetric());
            var condition = rule.describeCondition(measured);
            var instructions = AlertGuidanceCatalog.instructionsFor(rule.getMetric(), rule.getSeverity());
            var alert = new Alert(command, rule, measured, condition, instructions);
            if (historical) {
                alert.resolve();
            }
            alertRepository.saveAndFlush(alert);
            if (!historical) {
                createNotificationIfPossible(alert, command);
            }
            created.add(alert);
        }
        log.info("Telemetry device {} event {} evaluated against {} rules, {} alerts created, historical {}",
                device, event, rules.size(), created.size(), historical);
        return Result.success(created);
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
