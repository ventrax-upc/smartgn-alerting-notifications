package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.queryservices.NotificationQueryService;
import com.ventrax.alerting.notifications.domain.model.aggregates.Notification;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationAttemptsQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationByIdQuery;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeliveryStatus;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationAttemptResource;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationResource;
import com.ventrax.alerting.notifications.interfaces.transform.NotificationResourceFromEntityAssembler;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "Outbox and delivery history")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {

    private final NotificationQueryService notificationQueryService;
    private final RequestActor requestActor;
    private final MessageSource messageSource;

    public NotificationsController(
            NotificationQueryService notificationQueryService,
            RequestActor requestActor,
            MessageSource messageSource) {
        this.notificationQueryService = notificationQueryService;
        this.requestActor = requestActor;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List notifications",
            description = "Returns a paginated list of notifications visible to the caller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications found",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Long alertId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var accountId = requestActor.isSuperAdmin()
                ? null
                : requestActor.accountId().map(AccountId::new).orElse(null);
        var result = notificationQueryService.handle(
                new GetAllNotificationsQuery(accountId, alertId, status, page, size));
        var resources = result.content().stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok()
                .headers(PageHeaders.from(result))
                .body(resources);
    }

    @Operation(summary = "Get a notification by id",
            description = "Returns the status and recipient of a notification visible to the caller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found",
                    content = @Content(schema = @Schema(implementation = NotificationResource.class))),
            @ApiResponse(responseCode = "403", description = "Notification belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getById(@PathVariable Long notificationId) {
        var notification = notificationQueryService
                .handle(new GetNotificationByIdQuery(notificationId))
                .orElse(null);
        if (notification == null) {
            return ResponseEntityFromNotificationQueryResultAssembler
                    .notFound(messageSource);
        }
        if (!canAccess(notification)) {
            return ResponseEntityFromNotificationQueryResultAssembler
                    .forbidden(messageSource);
        }
        return ResponseEntityFromNotificationQueryResultAssembler
                .toResponseEntityFromNotification(notification);
    }

    @Operation(summary = "List delivery attempts for a notification",
            description = "Returns recorded attempts and provider outcomes for a notification visible to the caller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attempts found",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationAttemptResource.class)))),
            @ApiResponse(responseCode = "403", description = "Notification belongs to another account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{notificationId}/attempts")
    public ResponseEntity<?> getAttempts(@PathVariable Long notificationId) {
        var notification = notificationQueryService
                .handle(new GetNotificationByIdQuery(notificationId))
                .orElse(null);
        if (notification == null) {
            return ResponseEntityFromNotificationQueryResultAssembler
                    .notFound(messageSource);
        }
        if (!canAccess(notification)) {
            return ResponseEntityFromNotificationQueryResultAssembler
                    .forbidden(messageSource);
        }
        var attempts = notificationQueryService.handle(
                new GetNotificationAttemptsQuery(notificationId));
        return ResponseEntityFromNotificationQueryResultAssembler
                .toResponseEntityFromAttemptList(attempts);
    }

    private boolean canAccess(Notification notification) {
        var actor = requestActor.current();
        if (actor.isEmpty() || requestActor.isSuperAdmin()) {
            return true;
        }
        return notification.getAccountId().value().equals(actor.get().accountId());
    }
}
