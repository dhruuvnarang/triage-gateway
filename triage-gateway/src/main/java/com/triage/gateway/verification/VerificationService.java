package com.triage.gateway.verification;

import com.triage.gateway.audit.AuditOutcome;
import com.triage.gateway.audit.AuditService;
import com.triage.gateway.audit.VendorCallRecord;
import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.vendor.VendorAdapter;
import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.vendor.VendorRegistry;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * Runs one verification against the highest-priority adapter for its type and
 * audits the vendor call. Resilience and fallback are added in later phases;
 * for now a vendor failure propagates to the caller.
 */
@Service
public class VerificationService {

	private final VendorRegistry registry;
	private final AuditService auditService;
	private final Clock clock;

	public VerificationService(VendorRegistry registry, AuditService auditService, Clock clock) {
		this.registry = registry;
		this.auditService = auditService;
		this.clock = clock;
	}

	public VerificationResponse verify(VerificationType type, VerificationRequest request, String idempotencyKey) {
		List<VendorAdapter> candidates = registry.candidates(type);
		if (candidates.isEmpty()) {
			throw new UnsupportedVerificationException(type);
		}
		VendorAdapter adapter = candidates.get(0);

		long startNanos = System.nanoTime();
		try {
			VerificationResponse response = adapter.verify(request);
			audit(adapter, type, idempotencyKey, startNanos, AuditOutcome.SUCCESS, null);
			return response;
		} catch (VendorCallException e) {
			audit(adapter, type, idempotencyKey, startNanos, AuditOutcome.FAILURE, e.getMessage());
			throw e;
		}
	}

	private void audit(VendorAdapter adapter, VerificationType type, String idempotencyKey,
					   long startNanos, AuditOutcome outcome, String errorDetail) {
		long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;
		auditService.record(new VendorCallRecord(
				adapter.vendorId(),
				type.name(),
				idempotencyKey,
				latencyMs,
				outcome,
				errorDetail,
				null,
				null,
				clock.instant()));
	}
}
