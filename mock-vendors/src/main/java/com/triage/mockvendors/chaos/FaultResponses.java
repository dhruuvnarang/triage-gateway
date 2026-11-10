package com.triage.mockvendors.chaos;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Maps an injected fault to the HTTP response a real vendor would send.
 * Malformed responses are deliberately truncated JSON with a 200 status,
 * which is the failure mode that is hardest to detect.
 */
public final class FaultResponses {

	private FaultResponses() {
	}

	public static ResponseEntity<?> of(Fault fault) {
		return switch (fault) {
			case SERVER_ERROR -> ResponseEntity.status(503).body(error("SERVICE_UNAVAILABLE"));
			case RATE_LIMITED -> ResponseEntity.status(429).header("Retry-After", "1").body(error("RATE_LIMITED"));
			case TIMEOUT -> ResponseEntity.status(504).body(error("GATEWAY_TIMEOUT"));
			case MALFORMED -> ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body("{\"status\":\"succ");
			case NONE -> throw new IllegalArgumentException("NONE is not a fault response");
		};
	}

	private static Map<String, String> error(String code) {
		return Map.of("error", code);
	}
}
