package com.triage.gateway.vendor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "triage")
public record TriageProperties(Map<String, VendorProperties> vendors) {

	public TriageProperties {
		vendors = vendors == null ? Map.of() : Map.copyOf(vendors);
	}
}
