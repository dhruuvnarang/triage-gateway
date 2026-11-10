package com.triage.gateway.vendor;

import com.triage.gateway.model.VerificationType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds enabled adapters grouped by verification type, ordered by configured
 * priority. Adding a vendor needs an adapter bean and a {@code triage.vendors}
 * entry; nothing here changes.
 */
@Component
public class VendorRegistry {

	private final Map<VerificationType, List<VendorAdapter>> byType;

	public VendorRegistry(List<VendorAdapter> adapters, TriageProperties properties) {
		Map<String, VendorProperties> config = properties.vendors();
		this.byType = adapters.stream()
				.filter(adapter -> config.containsKey(adapter.vendorId()))
				.filter(adapter -> config.get(adapter.vendorId()).enabled())
				.sorted(Comparator.comparingInt(adapter -> config.get(adapter.vendorId()).priority()))
				.collect(Collectors.collectingAndThen(
						Collectors.groupingBy(VendorAdapter::type),
						map -> Map.copyOf(map)));
	}

	/** Enabled adapters for the type, highest priority first. Empty if none are configured. */
	public List<VendorAdapter> candidates(VerificationType type) {
		return byType.getOrDefault(type, List.of());
	}
}
