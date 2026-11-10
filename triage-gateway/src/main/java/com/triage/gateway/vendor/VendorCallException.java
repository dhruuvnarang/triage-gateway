package com.triage.gateway.vendor;

/**
 * A vendor call failed in a way the caller should treat as a vendor problem:
 * transport error, non-2xx status, or a payload that could not be parsed.
 */
public class VendorCallException extends RuntimeException {

	private final String vendorId;

	public VendorCallException(String vendorId, String message, Throwable cause) {
		super(message, cause);
		this.vendorId = vendorId;
	}

	public String vendorId() {
		return vendorId;
	}
}
