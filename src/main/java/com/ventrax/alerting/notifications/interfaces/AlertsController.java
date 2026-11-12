package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.AlertCommandService;
import com.ventrax.alerting.notifications.application.queryservices.AlertQueryService;
import com.ventrax.alerting.notifications.application.queryservices.NotificationQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.commands.AcknowledgeAlertCommand;
import com.ventrax.alerting.notifications.domain.model.commands.ResolveAlertCommand;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationsByAlertQuery;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertStatus;
import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;
import com.ventrax.alerting.notifications.interfaces.resources.AlertResource;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationResource;
import com.ventrax.alerting.notifications.interfaces.transform.AlertResourceFromEntityAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromAlertCommandResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromAlertQueryResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromNotificationQueryResultAssembler;
import com.ventrax.alerting.shared.infrastructure.security.RequestActor;
import com.ventrax.alerting.shared.interfaces.rest.PageHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Alerts", description = "Critical alert consultation and lifecycle")
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertsController {

    private final AlertQueryService alertQueryService;
    private final AlertCommandService alertCommandService;
    private final NotificationQueryService notificationQueryService;
    private final RequestActor requestActor;
    private final MessageSource messageSource;

    public AlertsController(
            AlertQueryService alertQueryService,
            AlertCommandService alertCommandService,
            NotificationQueryService notificationQueryService,
            RequestActor requestActor,
            MessageSource messageSource) {
        this.alertQueryService = alertQueryService;
        this.alertCommandService = alertCommandService;
        this.notificationQueryService = notificationQueryService;
        this.requestActor = requestActor;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List alerts",
            description = "Returns a paginated list of alerts visible to the caller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = AlertResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) UUID supplyPointId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var accountId = requestActor.isSuperAdmin()
                ? null
                : requestActor.accountId().map(AccountId::new).orElse(null);
        var query = new GetAllAlertsQuery(
                accountId,
                supplyPointId == null ? null : new SupplyPointId(supplyPointId),
                status,
                severity,
                from,
                to,
                page,
                size);
        var result = alertQueryService.handle(query);
        var resources = result.content().stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok()
                .headers(PageHeaders.from(result))
                .body(resources);
    }

    @Operation(summary = "Get an alert by id",
            description = "Returns the alert details when the caller can access its account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))),
            @ApiResponse(responseCode = "403", description = "Alert belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{alertId}")
    public ResponseEntity<?> getById(@PathVariable Long alertId) {
        var alert = alertQueryService.handle(new GetAlertByIdQuery(alertId)).orElse(null);
        if (alert == null) {
            return ResponseEntityFromAlertQueryResultAssembler.notFound(messageSource, alertId);
        }
        if (!canAccess(alert)) {
            return ResponseEntityFromAlertQueryResultAssembler.forbidden(messageSource);
        }
        return ResponseEntityFromAlertQueryResultAssembler.toResponseEntityFromAlert(alert);
    }

    @Operation(summary = "Acknowledge an alert",
            description = "Records that the alert was reviewed and leaves it open for resolution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert acknowledged",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))),
            @ApiResponse(responseCode = "403", description = "Alert belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Invalid alert state",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<?> acknowledge(@PathVariable Long alertId) {
        var alert = alertQueryService.handle(new GetAlertByIdQuery(alertId)).orElse(null);
        if (alert == null) {
            return ResponseEntityFromAlertQueryResultAssembler.notFound(messageSource, alertId);
        }
        if (!canAccess(alert)) {
            return ResponseEntityFromAlertQueryResultAssembler.forbidden(messageSource);
        }
        return ResponseEntityFromAlertCommandResultAssembler.toResponseEntityFromAlertResult(
                alertCommandService.handle(new AcknowledgeAlertCommand(alertId)),
                messageSource);
    }

    @Operation(summary = "Resolve an alert",
            description = "Closes an alert after the underlying condition has been addressed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert resolved",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))),
            @ApiResponse(responseCode = "403", description = "Alert belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Invalid alert state",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long alertId) {
        var alert = alertQueryService.handle(new GetAlertByIdQuery(alertId)).orElse(null);
        if (alert == null) {
            return ResponseEntityFromAlertQueryResultAssembler.notFound(messageSource, alertId);
        }
        if (!canAccess(alert)) {
            return ResponseEntityFromAlertQueryResultAssembler.forbidden(messageSource);
        }
        return ResponseEntityFromAlertCommandResultAssembler.toResponseEntityFromAlertResult(
                alertCommandService.handle(new ResolveAlertCommand(alertId)),
                messageSource);
    }

    @Operation(summary = "List notifications for an alert",
            description = "Returns the delivery records linked to an alert the caller can access.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications found",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationResource.class)))),
            @ApiResponse(responseCode = "403", description = "Alert belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{alertId}/notifications")
    public ResponseEntity<?> getNotifications(@PathVariable Long alertId) {
        var alert = alertQueryService.handle(new GetAlertByIdQuery(alertId)).orElse(null);
        if (alert == null) {
            return ResponseEntityFromAlertQueryResultAssembler.notFound(messageSource, alertId);
        }
        if (!canAccess(alert)) {
            return ResponseEntityFromAlertQueryResultAssembler.forbidden(messageSource);
        }
        var notifications = notificationQueryService.handle(
                new GetNotificationsByAlertQuery(alertId));
        return ResponseEntityFromNotificationQueryResultAssembler
                .toResponseEntityFromList(notifications);
    }

    private boolean canAccess(Alert alert) {
        var actor = requestActor.current();
        if (actor.isEmpty() || requestActor.isSuperAdmin()) {
            return true;
        }
        return alert.getAccountId().value().equals(actor.get().accountId());
    }
}
