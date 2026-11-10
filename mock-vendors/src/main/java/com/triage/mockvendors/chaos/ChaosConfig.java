package com.triage.mockvendors.chaos;

/**
 * Chaos settings for one vendor. Percentages are independent probabilities
 * that are rolled once per request, so they must sum to at most 100: the rest
 * of the traffic gets a healthy response.
 */
public record ChaosConfig(
		long latencyMs,
		int timeoutPercent,
		int errorPercent,
		int rateLimitPercent,
		int malformedPercent,
		long timeoutHoldMs) {

	public static final long DEFAULT_TIMEOUT_HOLD_MS = 60_000;

	public ChaosConfig {
		if (latencyMs < 0 || timeoutHoldMs < 0) {
			throw new IllegalArgumentException("latencyMs and timeoutHoldMs must be >= 0");
		}
		checkPercent("timeoutPercent", timeoutPercent);
		checkPercent("errorPercent", errorPercent);
		checkPercent("rateLimitPercent", rateLimitPercent);
		checkPercent("malformedPercent", malformedPercent);
		if (timeoutPercent + errorPercent + rateLimitPercent + malformedPercent > 100) {
			throw new IllegalArgumentException("fault percentages must sum to at most 100");
		}
	}

	public static ChaosConfig healthy() {
		return new ChaosConfig(0, 0, 0, 0, 0, DEFAULT_TIMEOUT_HOLD_MS);
	}

	private static void checkPercent(String name, int value) {
		if (value < 0 || value > 100) {
			throw new IllegalArgumentException(name + " must be between 0 and 100");
		}
	}
}
