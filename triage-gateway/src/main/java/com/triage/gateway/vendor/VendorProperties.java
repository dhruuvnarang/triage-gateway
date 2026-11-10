package com.triage.gateway.vendor;

import com.triage.gateway.model.VerificationType;

/**
 * One entry under {@code triage.vendors}. {@code priority} is the static order
 * used to pick a vendor; lower wins. Adaptive routing will refine this later.
 */
public record VendorProperties(VerificationType type, int priority, boolean enabled, String baseUrl) {
}
