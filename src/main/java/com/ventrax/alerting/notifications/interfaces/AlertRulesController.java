package com.ventrax.alerting.notifications.interfaces;

import com.ventrax.alerting.notifications.application.commandservices.AlertRuleCommandService;
import com.ventrax.alerting.notifications.application.queryservices.AlertRuleQueryService;
import com.ventrax.alerting.notifications.domain.model.queries.GetAlertRuleByIdQuery;
import com.ventrax.alerting.notifications.domain.model.queries.GetAllAlertRulesQuery;
import com.ventrax.alerting.notifications.interfaces.resources.AlertRuleResource;
import com.ventrax.alerting.notifications.interfaces.resources.CreateAlertRuleResource;
import com.ventrax.alerting.notifications.interfaces.resources.UpdateAlertRuleResource;
import com.ventrax.alerting.notifications.interfaces.transform.CreateAlertRuleCommandFromResourceAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromAlertRuleCommandResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.ResponseEntityFromAlertRuleQueryResultAssembler;
import com.ventrax.alerting.notifications.interfaces.transform.UpdateAlertRuleCommandFromResourceAssembler;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alert rules", description = "Administration of the rule engine")
@RestController
@RequestMapping("/api/v1/alert-rules")
public class AlertRulesController {

    private final AlertRuleCommandService alertRuleCommandService;
    private final AlertRuleQueryService alertRuleQueryService;
    private final RequestActor requestActor;
    private final MessageSource messageSource;

    public AlertRulesController(
            AlertRuleCommandService alertRuleCommandService,
            AlertRuleQueryService alertRuleQueryService,
            RequestActor requestActor,
            MessageSource messageSource) {
        this.alertRuleCommandService = alertRuleCommandService;
        this.alertRuleQueryService = alertRuleQueryService;
        this.requestActor = requestActor;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List alert rules")
    @ApiResponse(responseCode = "200", description = "Rules found",
            content = @Content(array = @ArraySchema(
                    schema = @Schema(implementation = AlertRuleResource.class))))
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Boolean active) {
        var rules = alertRuleQueryService.handle(new GetAllAlertRulesQuery(active));
        return ResponseEntityFromAlertRuleQueryResultAssembler
                .toResponseEntityFromList(rules);
    }

    @Operation(summary = "Get an alert rule by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule found",
                    content = @Content(schema = @Schema(implementation = AlertRuleResource.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{ruleId}")
    public ResponseEntity<?> getById(@PathVariable Long ruleId) {
        var rule = alertRuleQueryService.handle(new GetAlertRuleByIdQuery(ruleId)).orElse(null);
        if (rule == null) {
            return ResponseEntityFromAlertRuleQueryResultAssembler.notFound(messageSource, ruleId);
        }
        return ResponseEntityFromAlertRuleQueryResultAssembler
                .toResponseEntityFromAlertRule(rule);
    }

    @Operation(summary = "Create an alert rule", description = "Requires SUPERADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rule created",
                    content = @Content(schema = @Schema(implementation = AlertRuleResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rule definition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Administrator role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Rule name already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateAlertRuleResource resource) {
        requestActor.requireAdmin();
        var command = CreateAlertRuleCommandFromResourceAssembler
                .toCommandFromResource(resource);
        return ResponseEntityFromAlertRuleCommandResultAssembler
                .toResponseEntityFromCreateResult(
                        alertRuleCommandService.handle(command), messageSource);
    }

    @Operation(summary = "Update an alert rule",
            description = "Requires SUPERADMIN role. Accepts PUT or PATCH with the full rule payload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated",
                    content = @Content(schema = @Schema(implementation = AlertRuleResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rule definition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Administrator role required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Rule name already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @RequestMapping(value = "/{ruleId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateAlertRuleResource resource) {
        requestActor.requireAdmin();
        var command = UpdateAlertRuleCommandFromResourceAssembler
                .toCommandFromResource(ruleId, resource);
        return ResponseEntityFromAlertRuleCommandResultAssembler
                .toResponseEntityFromUpdateResult(
                        alertRuleCommandService.handle(command), messageSource);
    }
}
