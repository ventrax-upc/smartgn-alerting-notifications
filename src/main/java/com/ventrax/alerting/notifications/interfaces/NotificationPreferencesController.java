package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.NotificationPreferenceCommandService;
import com.ventrax.alerting.notifications.application.queryservices.NotificationPreferenceQueryService;
import com.ventrax.alerting.notifications.domain.model.queries.GetNotificationPreferencesByAccountQuery;
import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationPreferenceResource;
import com.ventrax.alerting.notifications.interfaces.resources.UpdateNotificationPreferenceResource;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromNotificationPreferenceCommandResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromNotificationPreferenceQueryResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.UpdateNotificationPreferenceCommandFromResourceAssembler;
import com.ventrax.alerting.shared.infrastructure.security.RequestActor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Tag(name = "Notification preferences", description = "Channel and recipient per notification type")
@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationPreferencesController {

    private final NotificationPreferenceCommandService notificationPreferenceCommandService;
    private final NotificationPreferenceQueryService notificationPreferenceQueryService;
    private final RequestActor requestActor;
    private final MessageSource messageSource;

    public NotificationPreferencesController(
            NotificationPreferenceCommandService notificationPreferenceCommandService,
            NotificationPreferenceQueryService notificationPreferenceQueryService,
            RequestActor requestActor,
            MessageSource messageSource) {
        this.notificationPreferenceCommandService = notificationPreferenceCommandService;
        this.notificationPreferenceQueryService = notificationPreferenceQueryService;
        this.requestActor = requestActor;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List notification preferences for an account",
            description = "When accountId is omitted, the authenticated account is used.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences found",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationPreferenceResource.class)))),
            @ApiResponse(responseCode = "403", description = "Account scope required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<?> getByAccount(@RequestParam(required = false) UUID accountId) {
        var targetAccountId = accountId != null
                ? accountId
                : requestActor.accountId().orElse(null);
        if (targetAccountId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Account scope required");
        }
        requestActor.requireAccount(targetAccountId);
        var preferences = notificationPreferenceQueryService.handle(
                new GetNotificationPreferencesByAccountQuery(new AccountId(targetAccountId)));
        return ResponseEntityFromNotificationPreferenceQueryResultAssembler
                .toResponseEntityFromList(preferences);
    }

    @Operation(summary = "Upsert a notification preference",
            description = "Creates or updates the channel and recipient for an account and notification type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preference saved",
                    content = @Content(schema = @Schema(
                            implementation = NotificationPreferenceResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid channel or recipient",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account scope required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping
    public ResponseEntity<?> upsert(
            @Valid @RequestBody UpdateNotificationPreferenceResource resource) {
        requestActor.requireAccount(resource.accountId());
        var command = UpdateNotificationPreferenceCommandFromResourceAssembler
                .toCommandFromResource(resource);
        return ResponseEntityFromNotificationPreferenceCommandResultAssembler
                .toResponseEntityFromResult(
                        notificationPreferenceCommandService.handle(command), messageSource);
    }
}
