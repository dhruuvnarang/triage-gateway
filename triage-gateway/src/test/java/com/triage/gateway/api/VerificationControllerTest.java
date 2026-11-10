package com.triage.gateway.api;

import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationStatus;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.verification.UnsupportedVerificationException;
import com.triage.gateway.verification.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationController.class)
class VerificationControllerTest {

	private static final String PAN_BODY = "{\"pan\":\"ABCDE1234F\"}";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private VerificationService verificationService;

	@Test
	void panVerificationPassesKeyAndReturnsNormalizedResponse() throws Exception {
		when(verificationService.verify(eq(VerificationType.PAN), any(), eq("key-1")))
				.thenReturn(new VerificationResponse("pan-a", VerificationType.PAN,
						VerificationStatus.VERIFIED, Map.of("holderName", "MOCK HOLDER")));

		mvc.perform(post("/v1/verify/pan")
						.header("Idempotency-Key", "key-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PAN_BODY))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("VERIFIED"))
				.andExpect(jsonPath("$.attributes.holderName").value("MOCK HOLDER"));
	}

	@Test
	void missingIdempotencyKeyIsBadRequest() throws Exception {
		mvc.perform(post("/v1/verify/pan")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PAN_BODY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MISSING_HEADER"));
	}

	@Test
	void blankIdempotencyKeyIsBadRequest() throws Exception {
		mvc.perform(post("/v1/verify/pan")
						.header("Idempotency-Key", "   ")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PAN_BODY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void invalidPanFormatIsValidationError() throws Exception {
		mvc.perform(post("/v1/verify/pan")
						.header("Idempotency-Key", "key-2")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"pan\":\"not-a-pan\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	@Test
	void vendorFailureIsBadGateway() throws Exception {
		when(verificationService.verify(eq(VerificationType.PAN), any(), eq("key-3")))
				.thenThrow(new VendorCallException("pan-a", "PAN-A call failed", null));

		mvc.perform(post("/v1/verify/pan")
						.header("Idempotency-Key", "key-3")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PAN_BODY))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.code").value("VENDOR_FAILURE"));
	}

	@Test
	void bureauWithNoVendorIsNotImplemented() throws Exception {
		when(verificationService.verify(eq(VerificationType.BUREAU), any(), eq("key-4")))
				.thenThrow(new UnsupportedVerificationException(VerificationType.BUREAU));

		mvc.perform(post("/v1/verify/bureau")
						.header("Idempotency-Key", "key-4")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"applicantId\":\"APP-1\",\"pan\":\"ABCDE1234F\"}"))
				.andExpect(status().isNotImplemented())
				.andExpect(jsonPath("$.code").value("NOT_IMPLEMENTED"));

		verify(verificationService).verify(eq(VerificationType.BUREAU), any(), eq("key-4"));
	}
}
