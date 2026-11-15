package com.triage.gateway.error;

/** Vendor answered 503. Retried, and counted by the circuit breaker. */
public class VendorUnavailableException extends VendorException {

	public VendorUnavailableException(String message) {
		super(message);
	}

}
