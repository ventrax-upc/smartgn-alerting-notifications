package com.ventrax.alerting.shared.interfaces.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI alertingNotificationsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SmartGN Alerting & Notifications API")
                .version("1.0.0")
                .description("""
                        Rule evaluation, critical alerts, transactional outbox and multi-channel
                        notification dispatch for SmartGN.
                        """)
                .contact(new Contact().name("Ventrax").email("team@ventrax.pe")));
    }
}
