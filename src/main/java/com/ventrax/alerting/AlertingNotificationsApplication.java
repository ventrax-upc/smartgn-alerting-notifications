package com.ventrax.alerting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class AlertingNotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertingNotificationsApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
