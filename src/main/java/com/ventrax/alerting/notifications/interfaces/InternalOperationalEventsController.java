package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandService;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationResource;
import com.ventrax.alerting.notifications.interfaces.resources.OperationalEventResource;
import com.ventrax.alerting.notifications.interfaces.transform.CreateOperationalNotificationCommandFromResourceAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromNotificationCommandResultAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal operational events",
        description = "Operational notices for assignments, appointments, payments and invoices")
@RestController
@RequestMapping("/internal/v1/operational-events")
public class InternalOperationalEventsController {

    private final NotificationCommandService notificationCommandService;
    private final MessageSource messageSource;

    public InternalOperationalEventsController(
            NotificationCommandService notificationCommandService,
            MessageSource messageSource) {
        this.notificationCommandService = notificationCommandService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Create an operational notice",
            description = "Creates notifications for assignments, appointments, payments and invoices (US51).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notifications created",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid operational event",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate operational notice",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "No active preference for the account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OperationalEventResource resource) {
        var command = CreateOperationalNotificationCommandFromResourceAssembler
                .toCommandFromResource(resource);
        return ResponseEntityFromNotificationCommandResultAssembler
                .toResponseEntityFromOperationalResult(
                        notificationCommandService.handle(command), messageSource);
    }
}
