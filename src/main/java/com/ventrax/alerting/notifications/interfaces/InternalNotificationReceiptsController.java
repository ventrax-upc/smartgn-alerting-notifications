package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.NotificationCommandService;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationReceiptResource;
import com.ventrax.alerting.notifications.interfaces.resources.NotificationResource;
import com.ventrax.alerting.notifications.interfaces.transform.RecordNotificationReceiptCommandFromResourceAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromNotificationCommandResultAssembler;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Internal receipts", description = "Asynchronous provider delivery receipts")
@RestController
@RequestMapping("/internal/v1/notification-receipts")
public class InternalNotificationReceiptsController {

    private final NotificationCommandService notificationCommandService;
    private final MessageSource messageSource;

    public InternalNotificationReceiptsController(
            NotificationCommandService notificationCommandService,
            MessageSource messageSource) {
        this.notificationCommandService = notificationCommandService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Record a provider delivery receipt",
            description = "Updates the notification state from an asynchronous provider callback.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipt recorded",
                    content = @Content(schema = @Schema(implementation = NotificationResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid receipt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> record(@Valid @RequestBody NotificationReceiptResource resource) {
        var command = RecordNotificationReceiptCommandFromResourceAssembler
                .toCommandFromResource(resource);
        return ResponseEntityFromNotificationCommandResultAssembler
                .toResponseEntityFromNotificationResult(
                        notificationCommandService.handle(command), messageSource);
    }
}
