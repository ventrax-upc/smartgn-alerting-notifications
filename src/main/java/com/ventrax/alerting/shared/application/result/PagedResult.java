package com.ventrax.alerting.shared.application.result;

import java.util.List;

/**
 * Paginated application-layer result.
 *
 * @param <T> element type
 * @since 1.0
 */
public record PagedResult<T>(List<T> content, long total, int page, int size) {

    public PagedResult {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
