package com.triage.mockvendors;

import com.triage.mockvendors.chaos.ChaosService;
import com.triage.mockvendors.chaos.Vendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MockVendorsApiTest {

	private static final String VALID_PAN = "ABCDE1234F";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ChaosService chaos;

	@AfterEach
	void resetChaos() {
		for (Vendor vendor : Vendor.values()) {
			chaos.reset(vendor);
		}
	}

	@Test
	void panVendorAReturnsFlatPayload() throws Exception {
		mvc.perform(post("/mock/pan-a/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"pan\":\"" + VALID_PAN + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true))
				.andExpect(jsonPath("$.name").value("MOCK HOLDER"));
	}

	@Test
	void panVendorBReturnsNestedPayloadWithDifferentFieldNames() throws Exception {
		mvc.perform(post("/mock/pan-b/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"panNumber\":\"BADPAN\"}}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.panStatus").value("INVALID"));
	}

	@Test
	void bureauScoreIsStableForSameApplicant() throws Exception {
		String body = "{\"applicantId\":\"APP-1\",\"pan\":\"" + VALID_PAN + "\"}";
		String first = mvc.perform(post("/mock/bureau/score").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String second = mvc.perform(post("/mock/bureau/score").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		org.junit.jupiter.api.Assertions.assertEquals(first, second);
	}

	@Test
	void injectedServerErrorReturns503() throws Exception {
		mvc.perform(put("/chaos/pan-a").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":0,\"errorPercent\":100,"
								+ "\"rateLimitPercent\":0,\"malformedPercent\":0,\"timeoutHoldMs\":0}"))
				.andExpect(status().isOk());

		mvc.perform(post("/mock/pan-a/verify").contentType(MediaType.APPLICATION_JSON)
						.content("{\"pan\":\"" + VALID_PAN + "\"}"))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	void injectedRateLimitReturns429WithRetryAfter() throws Exception {
		mvc.perform(put("/chaos/bureau").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":0,\"errorPercent\":0,"
								+ "\"rateLimitPercent\":100,\"malformedPercent\":0,\"timeoutHoldMs\":0}"))
				.andExpect(status().isOk());

		mvc.perform(post("/mock/bureau/score").contentType(MediaType.APPLICATION_JSON)
						.content("{\"applicantId\":\"APP-2\"}"))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().string("Retry-After", "1"));
	}

	@Test
	void injectedTimeoutReturns504() throws Exception {
		mvc.perform(put("/chaos/pan-b").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":100,\"errorPercent\":0,"
								+ "\"rateLimitPercent\":0,\"malformedPercent\":0,\"timeoutHoldMs\":0}"))
				.andExpect(status().isOk());

		mvc.perform(post("/mock/pan-b/verify").contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"panNumber\":\"" + VALID_PAN + "\"}}"))
				.andExpect(status().isGatewayTimeout());
	}

	@Test
	void injectedMalformedPayloadIsTruncatedJsonWith200() throws Exception {
		mvc.perform(put("/chaos/pan-a").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":0,\"errorPercent\":0,"
								+ "\"rateLimitPercent\":0,\"malformedPercent\":100,\"timeoutHoldMs\":0}"))
				.andExpect(status().isOk());

		mvc.perform(post("/mock/pan-a/verify").contentType(MediaType.APPLICATION_JSON)
						.content("{\"pan\":\"" + VALID_PAN + "\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("{\"status\":\"succ"));
	}

	@Test
	void chaosConfigWithTooManyPercentagesIsRejected() throws Exception {
		mvc.perform(put("/chaos/pan-a").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":0,\"errorPercent\":60,"
								+ "\"rateLimitPercent\":60,\"malformedPercent\":0,\"timeoutHoldMs\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unknownVendorReturns404() throws Exception {
		mvc.perform(get("/chaos/nope")).andExpect(status().isNotFound());
	}

	@Test
	void deleteResetsChaosToHealthy() throws Exception {
		mvc.perform(put("/chaos/bureau").contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":0,\"timeoutPercent\":0,\"errorPercent\":100,"
								+ "\"rateLimitPercent\":0,\"malformedPercent\":0,\"timeoutHoldMs\":0}"))
				.andExpect(status().isOk());

		mvc.perform(delete("/chaos/bureau")).andExpect(status().isNoContent());

		mvc.perform(get("/chaos/bureau"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errorPercent").value(0));
	}
}
