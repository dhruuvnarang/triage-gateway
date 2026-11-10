package com.triage.gateway.vendor.pan;

import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationStatus;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.vendor.TriageProperties;
import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.vendor.VendorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PanVendorAAdapterTest {

	private static final String URL = "http://pan-a.test/mock/pan-a/verify";

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final PanVendorAAdapter adapter = new PanVendorAAdapter(builder, new TriageProperties(Map.of(
			"pan-a", new VendorProperties(VerificationType.PAN, 1, true, "http://pan-a.test"))));

	private static final VerificationRequest REQUEST = new VerificationRequest(null, "ABCDE1234F");

	@Test
	void mapsSuccessfulVendorResponseToNormalizedModel() {
		server.expect(requestTo(URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"pan\":\"ABCDE1234F\"}"))
				.andRespond(withSuccess("{\"pan\":\"ABCDE1234F\",\"valid\":true,\"name\":\"MOCK HOLDER\"}",
						MediaType.APPLICATION_JSON));

		VerificationResponse response = adapter.verify(REQUEST);

		server.verify();
		assertEquals(VerificationStatus.VERIFIED, response.status());
		assertEquals("pan-a", response.vendorId());
		assertEquals("MOCK HOLDER", response.attributes().get("holderName"));
	}

	@Test
	void serverErrorIsWrappedAsVendorCallException() {
		server.expect(requestTo(URL)).andRespond(withServerError());

		VendorCallException e = assertThrows(VendorCallException.class, () -> adapter.verify(REQUEST));

		assertEquals("pan-a", e.vendorId());
	}

	@Test
	void truncatedPayloadIsWrappedAsVendorCallException() {
		server.expect(requestTo(URL))
				.andRespond(withSuccess("{\"status\":\"succ", MediaType.APPLICATION_JSON));

		assertThrows(VendorCallException.class, () -> adapter.verify(REQUEST));
	}
}
