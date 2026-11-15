package com.triage.mockvendors.chaos;

/** Per-vendor fault injection. errorPercent is 0-100. forceTimeout stalls the call for 30s. */
public record ChaosSettings(long latencyMs, int errorPercent, boolean forceTimeout) {

	public static final ChaosSettings NONE = new ChaosSettings(0, 0, false);

}
