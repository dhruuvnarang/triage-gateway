package com.triage.gateway.model;

/**
 * Outcome of a verification, independent of which vendor produced it.
 * PENDING_VERIFICATION means no vendor could answer and the request is queued.
 */
public enum VerificationStatus {
	VERIFIED,
	NOT_VERIFIED,
	PENDING_VERIFICATION
}
