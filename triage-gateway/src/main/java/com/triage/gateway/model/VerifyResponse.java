package com.triage.gateway.model;

/** Internal response: the same shape whichever vendor answered. */
public record VerifyResponse(String status, String vendor, boolean fromFallback) {
}
