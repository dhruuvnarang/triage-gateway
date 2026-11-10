package com.triage.gateway.vendor.pan;

import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationStatus;
import com.triage.gateway.model.VerificationType;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps PAN-A's wire payload to the internal model. Only vendor-neutral fields
 * are copied, and the PAN number itself is not echoed back.
 */
public class PanVendorANormalizer {

	public VerificationResponse normalize(String vendorId, PanVendorAResponse payload) {
		Map<String, String> attributes = new HashMap<>();
		if (payload.name() != null) {
			attributes.put("holderName", payload.name());
		}
		VerificationStatus status = payload.valid() ? VerificationStatus.VERIFIED : VerificationStatus.NOT_VERIFIED;
		return new VerificationResponse(vendorId, VerificationType.PAN, status, attributes);
	}
}
