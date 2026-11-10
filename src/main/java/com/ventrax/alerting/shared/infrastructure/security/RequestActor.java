package com.ventrax.alerting.shared.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class RequestActor {

    public Optional<Actor> current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Actor actor) {
            return Optional.of(actor);
        }
        return Optional.empty();
    }

    public Optional<UUID> accountId() {
        return current().map(Actor::accountId).filter(java.util.Objects::nonNull);
    }

    public boolean isSuperAdmin() {
        return current().map(Actor::isSuperAdmin).orElse(false);
    }

    public void requireAdmin() {
        if (current().isPresent() && !isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role required");
        }
    }

    public void requireAccount(UUID accountId) {
        if (current().isPresent() && !isSuperAdmin()) {
            var actorAccount = current().map(Actor::accountId).orElse(null);
            if (actorAccount == null || !actorAccount.equals(accountId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account scope required");
            }
        }
    }
}
