package com.triage.gateway.audit;

import java.time.Instant;

/**
 * Audit DTO passed from the verification flow to {@link AuditService}. Kept
 * separate from {@link VendorCallAudit} so the entity can change without
 * touching callers. Deliberately carries no PAN or applicant data.
 */
public record VendorCallRecord(
		String vendorId,
		String verificationType,
		String idempotencyKey,
		long latencyMs,
		AuditOutcome outcome,
		String errorDetail,
		String circuitState,
		Double healthScore,
		Instant occurredAt) {
}
