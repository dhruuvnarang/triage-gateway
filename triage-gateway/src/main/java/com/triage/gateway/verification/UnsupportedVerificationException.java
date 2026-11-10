package com.triage.gateway.verification;

import com.triage.gateway.model.VerificationType;

/** No enabled vendor is configured for this verification type. */
public class UnsupportedVerificationException extends RuntimeException {

	public UnsupportedVerificationException(VerificationType type) {
		super("No enabled vendor is configured for " + type);
	}
}
