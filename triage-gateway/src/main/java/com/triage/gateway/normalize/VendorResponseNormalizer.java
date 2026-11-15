package com.triage.gateway.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import com.triage.gateway.error.VendorException;
import org.springframework.stereotype.Component;

/** Maps each vendor's own payload shape to one internal status: VERIFIED or NOT_VERIFIED. */
@Component
public class VendorResponseNormalizer {

	/** Vendor A: {"verdict": "VALID" | "INVALID", ...} */
	public String fromVendorA(JsonNode payload) {
		return switch (payload.path("verdict").asText()) {
			case "VALID" -> "VERIFIED";
			case "INVALID" -> "NOT_VERIFIED";
			default -> throw new VendorException("unexpected vendor-a payload");
		};
	}

	/** Vendor B: {"result": {"code": "OK" | "FAIL"}, ...} */
	public String fromVendorB(JsonNode payload) {
		return switch (payload.path("result").path("code").asText()) {
			case "OK" -> "VERIFIED";
			case "FAIL" -> "NOT_VERIFIED";
			default -> throw new VendorException("unexpected vendor-b payload");
		};
	}

}
