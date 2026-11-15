package com.triage.gateway.normalize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triage.gateway.error.VendorException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendorResponseNormalizerTest {

	private final VendorResponseNormalizer normalizer = new VendorResponseNormalizer();
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void mapsVendorAVerdicts() throws Exception {
		assertThat(normalizer.fromVendorA(mapper.readTree("{\"verdict\":\"VALID\"}"))).isEqualTo("VERIFIED");
		assertThat(normalizer.fromVendorA(mapper.readTree("{\"verdict\":\"INVALID\"}"))).isEqualTo("NOT_VERIFIED");
	}

	@Test
	void mapsVendorBCodes() throws Exception {
		assertThat(normalizer.fromVendorB(mapper.readTree("{\"result\":{\"code\":\"OK\"}}"))).isEqualTo("VERIFIED");
		assertThat(normalizer.fromVendorB(mapper.readTree("{\"result\":{\"code\":\"FAIL\"}}"))).isEqualTo("NOT_VERIFIED");
	}

	@Test
	void unknownPayloadIsAVendorFailure() throws Exception {
		assertThatThrownBy(() -> normalizer.fromVendorA(mapper.readTree("{\"other\":1}")))
				.isInstanceOf(VendorException.class);
	}

}
