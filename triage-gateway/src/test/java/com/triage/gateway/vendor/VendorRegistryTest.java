package com.triage.gateway.vendor;

import com.triage.gateway.model.VerificationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VendorRegistryTest {

	@Test
	void ordersCandidatesByPriorityAscending() {
		VendorAdapter low = adapter("pan-low", VerificationType.PAN);
		VendorAdapter high = adapter("pan-high", VerificationType.PAN);
		TriageProperties props = new TriageProperties(Map.of(
				"pan-low", new VendorProperties(VerificationType.PAN, 2, true, "http://x"),
				"pan-high", new VendorProperties(VerificationType.PAN, 1, true, "http://y")));

		VendorRegistry registry = new VendorRegistry(List.of(low, high), props);

		assertEquals(List.of(high, low), registry.candidates(VerificationType.PAN));
	}

	@Test
	void excludesDisabledAndUnconfiguredAdapters() {
		VendorAdapter disabled = adapter("pan-off", VerificationType.PAN);
		VendorAdapter unconfigured = adapter("pan-unknown", VerificationType.PAN);
		TriageProperties props = new TriageProperties(Map.of(
				"pan-off", new VendorProperties(VerificationType.PAN, 1, false, "http://x")));

		VendorRegistry registry = new VendorRegistry(List.of(disabled, unconfigured), props);

		assertTrue(registry.candidates(VerificationType.PAN).isEmpty());
	}

	@Test
	void returnsEmptyForTypeWithNoAdapter() {
		VendorAdapter pan = adapter("pan-a", VerificationType.PAN);
		TriageProperties props = new TriageProperties(Map.of(
				"pan-a", new VendorProperties(VerificationType.PAN, 1, true, "http://x")));

		VendorRegistry registry = new VendorRegistry(List.of(pan), props);

		assertTrue(registry.candidates(VerificationType.BUREAU).isEmpty());
	}

	private static VendorAdapter adapter(String id, VerificationType type) {
		VendorAdapter adapter = mock(VendorAdapter.class);
		when(adapter.vendorId()).thenReturn(id);
		when(adapter.type()).thenReturn(type);
		return adapter;
	}
}
