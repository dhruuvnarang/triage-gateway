package com.triage.gateway.api;

import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.verification.VerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/verify")
public class VerificationController {

	static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

	private final VerificationService verificationService;

	public VerificationController(VerificationService verificationService) {
		this.verificationService = verificationService;
	}

	@PostMapping("/pan")
	public VerificationResponse verifyPan(
			@RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
			@Valid @RequestBody PanVerificationRequest body) {
		requireKey(idempotencyKey);
		return verificationService.verify(VerificationType.PAN,
				new VerificationRequest(null, body.pan()), idempotencyKey);
	}

	@PostMapping("/bureau")
	public VerificationResponse verifyBureau(
			@RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
			@Valid @RequestBody BureauVerificationRequest body) {
		requireKey(idempotencyKey);
		return verificationService.verify(VerificationType.BUREAU,
				new VerificationRequest(body.applicantId(), body.pan()), idempotencyKey);
	}

	private static void requireKey(String idempotencyKey) {
		if (idempotencyKey.isBlank()) {
			throw new InvalidRequestException(IDEMPOTENCY_KEY_HEADER + " must not be blank");
		}
	}

	public record PanVerificationRequest(
			@NotBlank @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "must be a valid PAN format") String pan) {
	}

	public record BureauVerificationRequest(
			@NotBlank String applicantId,
			@NotBlank @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "must be a valid PAN format") String pan) {
	}
}
