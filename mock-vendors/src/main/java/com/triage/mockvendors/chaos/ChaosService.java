package com.triage.mockvendors.chaos;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

@Service
public class ChaosService {

	private final Map<Vendor, ChaosConfig> configs = new ConcurrentHashMap<>();
	private final RandomGenerator random = RandomGenerator.getDefault();

	public ChaosService() {
		for (Vendor vendor : Vendor.values()) {
			configs.put(vendor, ChaosConfig.healthy());
		}
	}

	public ChaosConfig get(Vendor vendor) {
		return configs.get(vendor);
	}

	public void set(Vendor vendor, ChaosConfig config) {
		configs.put(vendor, config);
	}

	public void reset(Vendor vendor) {
		configs.put(vendor, ChaosConfig.healthy());
	}

	/**
	 * Applies latency, then rolls at most one fault for this request.
	 * Timeouts block for {@code timeoutHoldMs} before reporting, so the caller
	 * sees the request hang the way a real vendor timeout does.
	 */
	public Fault inject(Vendor vendor) {
		ChaosConfig config = configs.get(vendor);
		sleep(config.latencyMs());

		int roll = random.nextInt(100);
		int timeoutEnd = config.timeoutPercent();
		int errorEnd = timeoutEnd + config.errorPercent();
		int rateLimitEnd = errorEnd + config.rateLimitPercent();
		int malformedEnd = rateLimitEnd + config.malformedPercent();

		if (roll < timeoutEnd) {
			sleep(config.timeoutHoldMs());
			return Fault.TIMEOUT;
		}
		if (roll < errorEnd) {
			return Fault.SERVER_ERROR;
		}
		if (roll < rateLimitEnd) {
			return Fault.RATE_LIMITED;
		}
		if (roll < malformedEnd) {
			return Fault.MALFORMED;
		}
		return Fault.NONE;
	}

	private static void sleep(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
