package com.ventrax.alerting.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

/**
 * Trusted-gateway identity filter.
 *
 * <p>The API Gateway validates the JWT and forwards {@code X-Account-Id} and
 * {@code X-Role}. Internal service-to-service calls use a shared bearer token.
 * The gateway must strip client-supplied identity headers.</p>
 */
public class ActorAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCOUNT_HEADER = "X-Account-Id";
    private static final String ROLE_HEADER = "X-Role";

    private final SecurityProperties properties;

    public ActorAuthenticationFilter(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        var path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicInfrastructure(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/internal/")) {
            if (!validInternalToken(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal token");
                return;
            }
            authenticate(new Actor(null, "SYSTEM"));
            filterChain.doFilter(request, response);
            return;
        }

        var accountHeader = request.getHeader(ACCOUNT_HEADER);
        if (accountHeader == null || accountHeader.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing account identity");
            return;
        }
        UUID accountId;
        try {
            accountId = UUID.fromString(accountHeader);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid account identity");
            return;
        }
        var role = request.getHeader(ROLE_HEADER);
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        if (!"USER".equalsIgnoreCase(role) && !"SUPERADMIN".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid role");
            return;
        }
        authenticate(new Actor(accountId, role.toUpperCase()));
        filterChain.doFilter(request, response);
    }

    private boolean isPublicInfrastructure(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }

    private boolean validInternalToken(String authorization) {
        var configured = properties.internalToken();
        if (configured == null || configured.isBlank() || authorization == null) {
            return false;
        }
        var expected = "Bearer " + configured;
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                authorization.getBytes(StandardCharsets.UTF_8));
    }

    private void authenticate(Actor actor) {
        var authentication = new UsernamePasswordAuthenticationToken(
                actor,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + actor.role())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
