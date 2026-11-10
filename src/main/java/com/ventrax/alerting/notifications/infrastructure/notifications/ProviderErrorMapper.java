package com.ventrax.alerting.notifications.infrastructure.notifications;

import com.ventrax.alerting.notifications.application.outbound.NotificationDispatchResult;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpTimeoutException;

public final class ProviderErrorMapper {

    private ProviderErrorMapper() {
    }

    public static NotificationDispatchResult map(Exception exception) {
        if (exception instanceof HttpStatusCodeException http) {
            int status = http.getStatusCode().value();
            var message = truncate(http.getResponseBodyAsString());
            if (status == 408 || status == 429 || status >= 500) {
                return NotificationDispatchResult.transientFailure("HTTP_" + status, message);
            }
            return NotificationDispatchResult.permanentFailure("HTTP_" + status, message);
        }
        if (exception instanceof ResourceAccessException resource) {
            if (isTimeout(resource)) {
                return NotificationDispatchResult.indeterminate(
                        "TIMEOUT", "The provider did not answer before the configured timeout");
            }
            return NotificationDispatchResult.transientFailure(
                    "CONNECTION", "The provider connection failed");
        }
        return NotificationDispatchResult.indeterminate(
                "UNKNOWN", truncate(exception.getMessage()));
    }

    private static boolean isTimeout(Throwable throwable) {
        var current = throwable;
        while (current != null) {
            if (current instanceof HttpTimeoutException) return true;
            var message = current.getMessage();
            if (message != null && message.toLowerCase().contains("timed out")) return true;
            current = current.getCause();
        }
        return false;
    }

    private static String truncate(String value) {
        if (value == null) return null;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
