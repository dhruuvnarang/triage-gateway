package com.triage.gateway.error;

/** A failure the client should see: the status and code go into the error response. */
public class GatewayException extends RuntimeException {

	private final int status;
	private final String code;

	public GatewayException(int status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public int status() {
		return status;
	}

	public String code() {
		return code;
	}

}
