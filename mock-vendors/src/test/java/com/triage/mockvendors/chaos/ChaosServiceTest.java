package com.triage.mockvendors.chaos;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChaosServiceTest {

	private final ChaosService chaos = new ChaosService();

	@AfterEach
	void resetAll() {
		for (Vendor vendor : Vendor.values()) {
			chaos.reset(vendor);
		}
	}

	@Test
	void healthyVendorNeverFaults() {
		for (int i = 0; i < 200; i++) {
			assertEquals(Fault.NONE, chaos.inject(Vendor.PAN_A));
		}
	}

	@Test
	void fullErrorRateAlwaysReturnsServerError() {
		chaos.set(Vendor.BUREAU, new ChaosConfig(0, 0, 100, 0, 0, 0));
		for (int i = 0; i < 50; i++) {
			assertEquals(Fault.SERVER_ERROR, chaos.inject(Vendor.BUREAU));
		}
	}

	@Test
	void faultsOnlyAffectTheConfiguredVendor() {
		chaos.set(Vendor.PAN_A, new ChaosConfig(0, 0, 100, 0, 0, 0));
		assertEquals(Fault.NONE, chaos.inject(Vendor.PAN_B));
	}

	@Test
	void rejectsPercentagesThatExceedOneHundredInTotal() {
		assertThrows(IllegalArgumentException.class,
				() -> new ChaosConfig(0, 0, 60, 60, 0, 0));
	}

	@Test
	void rejectsOutOfRangePercentage() {
		assertThrows(IllegalArgumentException.class,
				() -> new ChaosConfig(0, -1, 0, 0, 0, 0));
	}
}
