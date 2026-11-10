package com.triage.gateway.vendor.pan;

import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationStatus;
import com.triage.gateway.model.VerificationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PanVendorANormalizerTest {

	private final PanVendorANormalizer normalizer = new PanVendorANormalizer();

	@Test
	void validPanBecomesVerifiedWithHolderName() {
		VerificationResponse response = normalizer.normalize("pan-a",
				new PanVendorAResponse("ABCDE1234F", true, "MOCK HOLDER"));

		assertEquals("pan-a", response.vendorId());
		assertEquals(VerificationType.PAN, response.type());
		assertEquals(VerificationStatus.VERIFIED, response.status());
		assertEquals("MOCK HOLDER", response.attributes().get("holderName"));
	}

	@Test
	void invalidPanBecomesNotVerifiedWithNoAttributes() {
		VerificationResponse response = normalizer.normalize("pan-a",
				new PanVendorAResponse("BADPAN", false, null));

		assertEquals(VerificationStatus.NOT_VERIFIED, response.status());
		assertFalse(response.attributes().containsKey("holderName"));
		assertFalse(response.attributes().containsValue("BADPAN"));
	}
}
