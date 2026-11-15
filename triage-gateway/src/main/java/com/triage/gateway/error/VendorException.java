package com.triage.gateway.error;

/** Base failure from a vendor call. Not retried, but counted by the circuit breaker. */
public class VendorException extends RuntimeException {

	public VendorException(String message) {
		super(message);
	}

	public VendorException(String message, Throwable cause) {
		super(message, cause);
	}

}
