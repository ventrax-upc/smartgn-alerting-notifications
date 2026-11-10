package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.AlertCommandService;
import com.ventrax.alerting.notifications.interfaces.resources.TelemetryEvaluationResource;
import com.ventrax.alerting.notifications.interfaces.resources.TelemetryEventResource;
import com.ventrax.alerting.notifications.interfaces.transform.EvaluateTelemetryCommandFromResourceAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromAlertCommandResultAssembler;
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

@Tag(name = "Internal telemetry", description = "Normalized telemetry events consumed from the broker")
@RestController
@RequestMapping("/internal/v1/telemetry-events")
public class InternalTelemetryEventsController {

    private final AlertCommandService alertCommandService;
    private final MessageSource messageSource;

    public InternalTelemetryEventsController(
            AlertCommandService alertCommandService, MessageSource messageSource) {
        this.alertCommandService = alertCommandService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Evaluate a normalized telemetry event",
            description = "Evaluates active rules and creates alerts plus outbox notifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation completed",
                    content = @Content(schema = @Schema(
                            implementation = TelemetryEvaluationResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid telemetry event",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> evaluate(@Valid @RequestBody TelemetryEventResource resource) {
        var command = EvaluateTelemetryCommandFromResourceAssembler
                .toCommandFromResource(resource);
        return ResponseEntityFromAlertCommandResultAssembler
                .toResponseEntityFromEvaluationResult(
                        alertCommandService.handle(command), messageSource);
    }
}
