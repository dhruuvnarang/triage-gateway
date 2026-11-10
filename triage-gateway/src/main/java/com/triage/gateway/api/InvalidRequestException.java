package com.triage.gateway.api;

/** Client sent a request that is malformed beyond bean validation (for example a blank idempotency key). */
public class InvalidRequestException extends RuntimeException {

	public InvalidRequestException(String message) {
		super(message);
	}
}
