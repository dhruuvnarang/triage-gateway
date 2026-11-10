package com.triage.mockvendors.pan;

import java.util.regex.Pattern;

/**
 * Shared PAN format check for both mock PAN vendors, so their verdicts agree
 * and only their payload shapes differ.
 */
final class PanRules {

	private static final Pattern PAN_FORMAT = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

	private PanRules() {
	}

	static boolean isValid(String pan) {
		return pan != null && PAN_FORMAT.matcher(pan).matches();
	}
}
