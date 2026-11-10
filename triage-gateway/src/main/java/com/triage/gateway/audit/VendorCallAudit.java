package com.triage.gateway.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "vendor_call_audit")
public class VendorCallAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vendor_id", nullable = false, length = 64)
	private String vendorId;

	@Column(name = "verification_type", nullable = false, length = 32)
	private String verificationType;

	@Column(name = "idempotency_key", nullable = false, length = 128)
	private String idempotencyKey;

	@Column(name = "latency_ms", nullable = false)
	private long latencyMs;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AuditOutcome outcome;

	@Column(name = "error_detail", length = 500)
	private String errorDetail;

	/** Null until the circuit breaker exists (Phase 3). */
	@Column(name = "circuit_state", length = 16)
	private String circuitState;

	/** Null until adaptive routing exists (Phase 4). */
	@Column(name = "health_score")
	private Double healthScore;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	protected VendorCallAudit() {
	}

	VendorCallAudit(VendorCallRecord record) {
		this.vendorId = record.vendorId();
		this.verificationType = record.verificationType();
		this.idempotencyKey = record.idempotencyKey();
		this.latencyMs = record.latencyMs();
		this.outcome = record.outcome();
		this.errorDetail = record.errorDetail();
		this.circuitState = record.circuitState();
		this.healthScore = record.healthScore();
		this.occurredAt = record.occurredAt();
	}

	public Long getId() {
		return id;
	}

	public String getVendorId() {
		return vendorId;
	}

	public String getVerificationType() {
		return verificationType;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public long getLatencyMs() {
		return latencyMs;
	}

	public AuditOutcome getOutcome() {
		return outcome;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public String getCircuitState() {
		return circuitState;
	}

	public Double getHealthScore() {
		return healthScore;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
