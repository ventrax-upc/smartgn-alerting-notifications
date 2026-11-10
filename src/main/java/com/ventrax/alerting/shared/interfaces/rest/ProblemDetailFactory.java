package com.ventrax.alerting.shared.interfaces.rest;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public final class ProblemDetailFactory {

    private ProblemDetailFactory() {
    }

    public static ProblemDetail of(HttpStatus status, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        // Compatibility alias for clients that read `details` instead of `detail`.
        problem.setProperty("details", detail);
        return problem;
    }

    public static String message(MessageSource messageSource, String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    public static ResponseEntity<?> response(
            HttpStatus status, String key, MessageSource messageSource, Object... args) {
        return ResponseEntity.status(status)
                .body(of(status, message(messageSource, key, args)));
    }
}
