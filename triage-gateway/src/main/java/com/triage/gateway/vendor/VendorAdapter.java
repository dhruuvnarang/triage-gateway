package com.triage.gateway.vendor;

import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationType;

/**
 * One implementation per vendor. An adapter owns the vendor's wire format:
 * it builds the vendor request, parses the vendor payload, and returns a
 * normalized {@link VerificationResponse}. Resilience and routing wrap this
 * call from the outside, so adapters contain no retry or fallback logic.
 */
public interface VendorAdapter {

	String vendorId();

	VerificationType type();

	VerificationResponse verify(VerificationRequest request);
}
