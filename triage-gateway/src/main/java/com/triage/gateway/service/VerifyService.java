package com.triage.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.triage.gateway.error.GatewayException;
import com.triage.gateway.idempotency.IdempotencyStore;
import com.triage.gateway.model.VerifyResponse;
import com.triage.gateway.vendor.PrimaryVendorClient;
import com.triage.gateway.vendor.SecondaryVendorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Idempotency first, then primary, then secondary as fallback. */
@Service
public class VerifyService {

	private static final Logger log = LoggerFactory.getLogger(VerifyService.class);

	private final PrimaryVendorClient primary;
	private final SecondaryVendorClient secondary;
	private final IdempotencyStore idempotency;

	public VerifyService(PrimaryVendorClient primary, SecondaryVendorClient secondary,
			IdempotencyStore idempotency) {
		this.primary = primary;
		this.secondary = secondary;
		this.idempotency = idempotency;
	}

	public VerifyResponse verify(String key, JsonNode body) {
		Optional<VerifyResponse> replay = idempotency.claimOrReplay(key);
		if (replay.isPresent()) {
			log.info("idempotent replay key={}", key);
			return replay.get();
		}
		try {
			VerifyResponse response = callWithFallback(body);
			idempotency.complete(key, response);
			return response;
		}
		catch (RuntimeException e) {
			idempotency.release(key);
			throw e;
		}
	}

	private VerifyResponse callWithFallback(JsonNode body) {
		try {
			return new VerifyResponse(primary.verify(body).join(), primary.name(), false);
		}
		catch (RuntimeException e) {
			log.warn("primary failed, trying secondary: {}", e.getMessage());
		}
		try {
			return new VerifyResponse(secondary.verify(body).join(), secondary.name(), true);
		}
		catch (RuntimeException e) {
			log.error("all vendors failed: {}", e.getMessage());
			throw new GatewayException(503, "ALL_VENDORS_UNAVAILABLE", "no vendor could verify the request");
		}
	}

}
