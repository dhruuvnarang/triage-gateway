package com.triage.gateway.vendor.pan;

import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.vendor.TriageProperties;
import com.triage.gateway.vendor.VendorAdapter;
import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.vendor.VendorProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adapter for the PAN-A mock vendor. The vendor id and base URL come from
 * {@code triage.vendors.pan-a}.
 */
@Component
public class PanVendorAAdapter implements VendorAdapter {

	static final String VENDOR_ID = "pan-a";

	private final RestClient client;
	private final PanVendorANormalizer normalizer = new PanVendorANormalizer();

	public PanVendorAAdapter(RestClient.Builder builder, TriageProperties properties) {
		VendorProperties config = properties.vendors().get(VENDOR_ID);
		if (config == null) {
			throw new IllegalStateException("Missing triage.vendors." + VENDOR_ID + " configuration");
		}
		this.client = builder.baseUrl(config.baseUrl()).build();
	}

	@Override
	public String vendorId() {
		return VENDOR_ID;
	}

	@Override
	public VerificationType type() {
		return VerificationType.PAN;
	}

	@Override
	public VerificationResponse verify(VerificationRequest request) {
		PanVendorAResponse payload;
		try {
			payload = client.post()
					.uri("/mock/pan-a/verify")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new PanVendorARequest(request.pan()))
					.retrieve()
					.body(PanVendorAResponse.class);
		} catch (RestClientException e) {
			throw new VendorCallException(VENDOR_ID, "PAN-A call failed: " + e.getMessage(), e);
		}
		if (payload == null) {
			throw new VendorCallException(VENDOR_ID, "PAN-A returned an empty body", null);
		}
		return normalizer.normalize(VENDOR_ID, payload);
	}
}
