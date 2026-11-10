package com.triage.gateway.model;

/**
 * Vendor-neutral input to a verification. {@code applicantId} is required for
 * BUREAU lookups; PAN checks use only {@code pan}.
 */
public record VerificationRequest(String applicantId, String pan) {
}
