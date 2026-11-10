package com.triage.gateway.api;

/** The single error shape every endpoint returns: a stable machine code and a human message. */
public record ApiError(String code, String message) {
}
