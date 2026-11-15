package com.triage.mockvendors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MockVendorControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void vendorAAnswersWithItsOwnPayloadWhenNoChaos() throws Exception {
		setChaos("vendor-a", 0, 0);
		mvc.perform(post("/mock/vendor-a/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.verdict").value("VALID"));
	}

	@Test
	void vendorBAnswersWithItsOwnPayloadWhenNoChaos() throws Exception {
		setChaos("vendor-b", 0, 0);
		mvc.perform(post("/mock/vendor-b/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result.code").value("OK"));
	}

	@Test
	void fullErrorPercentReturns503() throws Exception {
		setChaos("vendor-a", 0, 100);
		mvc.perform(post("/mock/vendor-a/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isServiceUnavailable());
		setChaos("vendor-a", 0, 0);
	}

	@Test
	void unknownVendorReturns404() throws Exception {
		mvc.perform(post("/mock/nope/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNotFound());
	}

	private void setChaos(String vendor, long latencyMs, int errorPercent) throws Exception {
		mvc.perform(put("/chaos/" + vendor).contentType(MediaType.APPLICATION_JSON)
						.content("{\"latencyMs\":" + latencyMs + ",\"errorPercent\":" + errorPercent
								+ ",\"forceTimeout\":false}"))
				.andExpect(status().isOk());
	}

}
