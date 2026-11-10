package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import com.ventrax.alerting.notifications.domain.model.aggregates.NotificationPreference;
import com.ventrax.alerting.notifications.domain.model.commands.CreateAlertRuleCommand;
import com.ventrax.alerting.notifications.domain.model.commands.UpdateNotificationPreferenceCommand;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationChannel;
import com.ventrax.alerting.notifications.domain.model.valueobjects.NotificationType;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleOperator;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.AlertRuleRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationPreferenceRepository;
import com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import com.ventrax.alerting.shared.infrastructure.security.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TelemetryAlertFlowIntegrationTest {

    private static final UUID ACCOUNT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

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
                "Gas detected test",
                RuleMetric.GAS_PRESENCE,
                RuleOperator.IS_TRUE,
                null,
                AlertSeverity.CRITICAL,
                "Test rule",
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
    void createsAlertAndPendingNotification() throws Exception {
        var propertyId = UUID.randomUUID();
        var supplyPointId = UUID.randomUUID();
        var body = telemetryBody(propertyId, supplyPointId, "evt-1", "dev-1");

        mockMvc.perform(post("/internal/v1/telemetry-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertsCreated").value(1));

        assertEquals(1, alertRepository.count());
        assertEquals(1, notificationRepository.count());
        var notification = notificationRepository.findAll().getFirst();
        assertEquals(DeliveryStatus.PENDING, notification.getStatus());
        assertEquals(NotificationChannel.EMAIL, notification.getChannel());
    }

    @Test
    void deduplicatesTelemetryEventByDeviceEventAndRule() throws Exception {
        var propertyId = UUID.randomUUID();
        var supplyPointId = UUID.randomUUID();
        var body = telemetryBody(propertyId, supplyPointId, "evt-dup", "dev-dup");

        mockMvc.perform(post("/internal/v1/telemetry-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertsCreated").value(1));

        mockMvc.perform(post("/internal/v1/telemetry-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertsCreated").value(0));

        assertEquals(1, alertRepository.count());
        assertEquals(1, notificationRepository.count());
    }

    private String telemetryBody(
            UUID propertyId, UUID supplyPointId, String eventId, String deviceId) {
        return """
                {
                  "eventId": "%s",
                  "deviceId": "%s",
                  "propertyId": "%s",
                  "supplyPointId": "%s",
                  "accountId": "%s",
                  "measuredAt": "%s",
                  "gasPresence": true
                }
                """.formatted(eventId, deviceId, propertyId, supplyPointId, ACCOUNT, Instant.now());
    }

    @Test
    void updatesAlertRuleWithPatch() throws Exception {
        var rule = alertRuleRepository.findAll().getFirst();
        var body = """
                {
                  "name": "Gas detected test",
                  "metric": "GAS_PRESENCE",
                  "operator": "IS_TRUE",
                  "severity": "CRITICAL",
                  "description": "Patched rule",
                  "active": true
                }
                """;

        mockMvc.perform(patch("/api/v1/alert-rules/{ruleId}", rule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Patched rule"))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void exposesProblemDetailDetails() throws Exception {
        mockMvc.perform(get("/api/v1/alert-rules/{ruleId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Alert rule not found for id 999999."))
                .andExpect(jsonPath("$.details")
                        .value("Alert rule not found for id 999999."));
    }

    @Test
    void usesAuthenticatedAccountWhenPreferenceAccountIdIsOmitted() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken(
                new Actor(ACCOUNT, "USER"), null, List.of());

        mockMvc.perform(get("/api/v1/notification-preferences")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(ACCOUNT.toString()));
    }
}
