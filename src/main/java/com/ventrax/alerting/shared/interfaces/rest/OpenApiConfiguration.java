package com.ventrax.alerting.shared.interfaces.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI alertingNotificationsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SmartGN - Alerting & Notifications")
                .version("v1")
                .description("Telemetry-based alert rules, alert acknowledgment and resolution, "
                        + "notification preferences and delivery tracking. Stores alerts and delivery history in PostgreSQL. "
                        + "Local Docker Compose uses a simulated notification provider.")
                .license(new License().name("MIT")))
                .components(new Components()
                        .addSecuritySchemes("serviceAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer")
                                .description("Internal service token."))
                        .addSecuritySchemes("gatewayIdentity", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
                                .name("X-Account-Id")
                                .description("Trusted Gateway account header. SPA clients authenticate with a JWT at the Gateway.")));
    }

    @Bean
    public OpenApiCustomizer alertingOperationSecurity() {
        return api -> api.getPaths().forEach((path, item) -> {
            if (path.startsWith("/internal/") || path.startsWith("/api/v1/")) {
                String scheme = path.startsWith("/internal/") ? "serviceAuth" : "gatewayIdentity";
                item.readOperations().forEach(operation -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(scheme));
                    operation.getResponses().putIfAbsent("401", new ApiResponse()
                            .description("Service authentication or trusted Gateway identity is missing or invalid."));
                });
            }
        });
    }
}
