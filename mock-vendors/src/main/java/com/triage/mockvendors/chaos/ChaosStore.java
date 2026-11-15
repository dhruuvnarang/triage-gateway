package com.triage.mockvendors.chaos;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChaosStore {

	private final Map<String, ChaosSettings> settings = new ConcurrentHashMap<>();

	public ChaosSettings get(String vendor) {
		return settings.getOrDefault(vendor, ChaosSettings.NONE);
	}

	public void set(String vendor, ChaosSettings value) {
		settings.put(vendor, value);
	}

}
