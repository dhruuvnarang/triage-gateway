package com.triage.gateway.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.triage.gateway.model.VerifyResponse;
import com.triage.gateway.service.VerifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerifyController {

	private final VerifyService service;

	public VerifyController(VerifyService service) {
		this.service = service;
	}

	@PostMapping("/v1/verify")
	public ResponseEntity<VerifyResponse> verify(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestBody JsonNode body) {
		if (idempotencyKey.isBlank()) {
			throw new IllegalArgumentException("Idempotency-Key must not be blank");
		}
		return ResponseEntity.ok(service.verify(idempotencyKey, body));
	}

}
