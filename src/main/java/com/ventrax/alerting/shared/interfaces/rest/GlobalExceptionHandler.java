package com.ventrax.alerting.shared.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception, Locale locale) {
        String prefix = messageSource.getMessage("errors.found", null, locale);
        String fields = exception.getFieldErrors().stream()
                .map(fieldError -> messageSource.getMessage(fieldError, locale))
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetailFactory.of(
                        HttpStatus.BAD_REQUEST, prefix + " " + fields));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException exception, Locale locale) {
        String messageKey = exception.getMessage() != null
                ? exception.getMessage()
                : "errors.found";
        String message = Objects.requireNonNullElse(
                messageSource.getMessage(messageKey, null, messageKey, locale),
                messageKey);
        log.warn("Illegal argument: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetailFactory.of(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ProblemDetail> handleIllegalState(
            IllegalStateException exception, Locale locale) {
        String messageKey = exception.getMessage() != null
                ? exception.getMessage()
                : "errors.found";
        String message = Objects.requireNonNullElse(
                messageSource.getMessage(messageKey, null, messageKey, locale),
                messageKey);
        log.warn("Invalid state: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetailFactory.of(HttpStatus.CONFLICT, message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleDataIntegrity(
            DataIntegrityViolationException exception, Locale locale) {
        String message = messageSource.getMessage("errors.conflict", null, locale);
        log.warn("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetailFactory.of(HttpStatus.CONFLICT, message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        var detail = exception.getReason() == null
                ? status.getReasonPhrase()
                : exception.getReason();
        return ResponseEntity.status(status)
                .body(ProblemDetailFactory.of(status, detail));
    }
}
