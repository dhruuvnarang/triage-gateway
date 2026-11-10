package com.triage.gateway.model;

import java.util.Map;

/**
 * The single internal response model every adapter normalizes into.
 * {@code attributes} carries vendor-neutral details whose keys are defined by
 * the verification type (for example {@code holderName} for PAN, {@code score}
 * and {@code band} for BUREAU), so adding a vendor never changes this record.
 */
public record VerificationResponse(
		String vendorId,
		VerificationType type,
		VerificationStatus status,
		Map<String, String> attributes) {

	public VerificationResponse {
		attributes = Map.copyOf(attributes);
	}
}
