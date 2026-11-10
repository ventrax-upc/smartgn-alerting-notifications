package com.ventrax.alerting.shared.interfaces.rest;

import com.ventrax.alerting.shared.application.result.PagedResult;
import org.springframework.http.HttpHeaders;

public final class PageHeaders {

    private PageHeaders() {
    }

    public static HttpHeaders from(PagedResult<?> result) {
        var headers = new HttpHeaders();
        headers.set("X-Total-Count", Long.toString(result.total()));
        headers.set("X-Page", Integer.toString(result.page()));
        headers.set("X-Page-Size", Integer.toString(result.size()));
        return headers;
    }
}
