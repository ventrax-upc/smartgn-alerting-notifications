package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.AlertCommandService;
import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandService;
import com.ventrax.alerting.notifications.application.commandservices.NotificationPreferenceCommandService;
import com.ventrax.alerting.notifications.application.queryservices.AlertQueryService;
import com.ventrax.alerting.notifications.application.queryservices.NotificationPreferenceQueryService;
import com.ventrax.alerting.notifications.application.queryservices.NotificationQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.commands.AcknowledgeAlertCommand;
import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.CreateOperationalNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.DispatchNotificationCommand;
import com.ventrax.alerting.notifications.domain.model.commands.EvaluateTelemetryCommand;
import com.ventrax.alerting.notifications.domain.model.commands.RecordNotificationReceiptCommand;
import com.ventrax.alerting.notifications.domain.model.commands.ResolveAlertCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationAttemptsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationPreferencesByAccountQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetPendingNotificationsQuery;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeviceId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import com.ventrax.alerting.notifications.domain.model.valueobjects.OperationalEventType;
import com.ventrax.alerting.notifications.domain.model.valueobjects.PropertyId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationLifecycleIntegrationTest {

    private static final UUID ACCOUNT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private AlertCommandService alertCommandService;

    @Autowired
    private AlertQueryService alertQueryService;

    @Autowired
    private NotificationCommandService notificationCommandService;

    @Autowired
    private NotificationQueryService notificationQueryService;

    @Autowired
    private NotificationPreferenceCommandService notificationPreferenceCommandService;

    @Autowired
    private NotificationPreferenceQueryService notificationPreferenceQueryService;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        alertRepository.deleteAll();
        notificationPreferenceRepository.deleteAll();
        alertRuleRepository.deleteAll();

        alertRuleRepository.save(new AlertRule(new CreateAlertRuleCommand(
                "Gas lifecycle test",
                RuleMetric.GAS_PRESENCE,
                RuleOperator.IS_TRUE,
                null,
                AlertSeverity.CRITICAL,
                "Lifecycle rule",
                true)));

        notificationPreferenceRepository.save(new NotificationPreference(
                new UpdateNotificationPreferenceCommand(
                        new AccountId(ACCOUNT),
                        NotificationType.CRITICAL_ALERT,
                        NotificationChannel.EMAIL,
                        "ops@smartgn.pe",
                        true)));
    }

    @Test
    void completesAlertAndNotificationLifecycle() {
        var propertyId = UUID.randomUUID();
        var supplyPointId = UUID.randomUUID();
        var evaluation = alertCommandService.handle(new EvaluateTelemetryCommand(
                new EventId("evt-lifecycle-1"),
                new DeviceId("dev-lifecycle-1"),
                new PropertyId(propertyId),
                new SupplyPointId(supplyPointId),
                new AccountId(ACCOUNT),
                Instant.now(),
                null,
                null,
                null,
                true,
                null,
                "ops@smartgn.pe",
                null));

        assertTrue(evaluation.isSuccess());
        var alert = evaluation.success().orElseThrow().getFirst();

        var alertsPage = alertQueryService.handle(new GetAllAlertsQuery(
                new AccountId(ACCOUNT), null, null, null, null, null, 0, 20));
        assertEquals(1, alertsPage.content().size());

        var pending = notificationQueryService.handle(new GetPendingNotificationsQuery(10));
        assertEquals(1, pending.size());
        var notification = pending.getFirst();

        var dispatched = notificationCommandService.handle(
                new DispatchNotificationCommand(notification.getId()));
        assertTrue(dispatched.isSuccess());
        var accepted = dispatched.success().orElseThrow();
        assertEquals(DeliveryStatus.ACCEPTED, accepted.getStatus());
        assertEquals(1, accepted.getAttempts());
        assertTrue(accepted.getProviderMessageId().startsWith("sim-"));

        var attempts = notificationQueryService.handle(
                new GetNotificationAttemptsQuery(notification.getId()));
        assertEquals(1, attempts.size());
        assertEquals(DeliveryStatus.ACCEPTED, attempts.getFirst().getStatus());

        var receipt = notificationCommandService.handle(new RecordNotificationReceiptCommand(
                accepted.getProviderMessageId(),
                DeliveryStatus.DELIVERED,
                null,
                null));
        assertTrue(receipt.isSuccess());
        assertEquals(DeliveryStatus.DELIVERED,
                receipt.success().orElseThrow().getStatus());

        var acknowledged = alertCommandService.handle(
                new AcknowledgeAlertCommand(alert.getId()));
        assertTrue(acknowledged.isSuccess());
        assertEquals(AlertStatus.ACKNOWLEDGED,
                acknowledged.success().orElseThrow().getStatus());

        var resolved = alertCommandService.handle(new ResolveAlertCommand(alert.getId()));
        assertTrue(resolved.isSuccess());
        assertEquals(AlertStatus.RESOLVED,
                resolved.success().orElseThrow().getStatus());

        var notificationsPage = notificationQueryService.handle(
                new GetAllNotificationsQuery(new AccountId(ACCOUNT), null, null, 0, 20));
        assertEquals(1, notificationsPage.content().size());
    }

    @Test
    void upsertsNotificationPreference() {
        var updated = notificationPreferenceCommandService.handle(
                new UpdateNotificationPreferenceCommand(
                        new AccountId(ACCOUNT),
                        NotificationType.CRITICAL_ALERT,
                        NotificationChannel.SMS,
                        "+51999999999",
                        true));

        assertTrue(updated.isSuccess());
        assertEquals(NotificationChannel.SMS, updated.success().orElseThrow().getChannel());

        var preferences = notificationPreferenceQueryService.handle(
                new GetNotificationPreferencesByAccountQuery(new AccountId(ACCOUNT)));
        assertEquals(1, preferences.size());
        assertEquals(NotificationChannel.SMS, preferences.getFirst().getChannel());
        assertEquals("+51999999999", preferences.getFirst().getRecipient().value());
    }

    @Test
    void createsOperationalNoticeAndRejectsDuplicate() {
        var preference = notificationPreferenceCommandService.handle(
                new UpdateNotificationPreferenceCommand(
                        new AccountId(ACCOUNT),
                        NotificationType.OPERATIONAL_NOTICE,
                        NotificationChannel.EMAIL,
                        "ops@smartgn.pe",
                        true));
        assertTrue(preference.isSuccess());

        var command = new CreateOperationalNotificationCommand(
                new EventId("op-2026-0042"),
                new AccountId(ACCOUNT),
                OperationalEventType.PAYMENT_CONFIRMED,
                "Pago confirmado",
                "Su pago fue registrado correctamente.");

        var created = notificationCommandService.handle(command);
        assertTrue(created.isSuccess());
        assertEquals(1, created.success().orElseThrow().size());
        assertEquals(NotificationType.OPERATIONAL_NOTICE,
                created.success().orElseThrow().getFirst().getNotificationType());
        assertEquals(OperationalEventType.PAYMENT_CONFIRMED,
                created.success().orElseThrow().getFirst().getOperationalEventType());

        var duplicate = notificationCommandService.handle(command);
        assertTrue(duplicate.isFailure());
    }

    @Test
    void updatePreferencePreservesActiveWhenNotProvided() {
        var preference = new NotificationPreference(
                new UpdateNotificationPreferenceCommand(
                        new AccountId(ACCOUNT),
                        NotificationType.OPERATIONAL_NOTICE,
                        NotificationChannel.EMAIL,
                        "ops@smartgn.pe",
                        false));
        notificationPreferenceRepository.save(preference);

        preference.update(new UpdateNotificationPreferenceCommand(
                new AccountId(ACCOUNT),
                NotificationType.OPERATIONAL_NOTICE,
                NotificationChannel.SMS,
                "+51999999999",
                null));

        assertFalse(preference.isActive());
        assertEquals(NotificationChannel.SMS, preference.getChannel());
    }
}
