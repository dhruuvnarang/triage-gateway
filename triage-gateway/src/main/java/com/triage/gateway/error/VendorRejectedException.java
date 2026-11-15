package com.triage.gateway.error;

/** Vendor answered 4xx. Never retried, and ignored by the circuit breaker. */
public class VendorRejectedException extends VendorException {

	public VendorRejectedException(String message) {
		super(message);
	}

}
