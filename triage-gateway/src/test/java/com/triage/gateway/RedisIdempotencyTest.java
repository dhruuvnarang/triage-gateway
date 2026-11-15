package com.triage.gateway;

import com.triage.gateway.support.StubVendor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Runs against a real Redis in Docker. Skipped automatically when Docker is not available. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class RedisIdempotencyTest {

	@Container
	static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

	private static final StubVendor PRIMARY = StubVendor.start("{\"verdict\":\"VALID\"}");
	private static final StubVendor SECONDARY = StubVendor.start("{\"result\":{\"code\":\"OK\"}}");

	@Autowired
	private MockMvc mvc;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
		registry.add("triage.vendors.primary.url", PRIMARY::url);
		registry.add("triage.vendors.secondary.url", SECONDARY::url);
	}

	@AfterAll
	static void stopVendors() {
		PRIMARY.stop();
		SECONDARY.stop();
	}

	@BeforeEach
	void resetVendors() {
		PRIMARY.reset(200);
		SECONDARY.reset(200);
	}

	@Test
	void sequentialDuplicateReturnsStoredResponseWithoutVendorCall() throws Exception {
		String key = UUID.randomUUID().toString();
		verify(key).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
		verify(key).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
		assertThat(PRIMARY.hits()).isEqualTo(1);
	}

	@Test
	void simultaneousDuplicatesCauseOneVendorCallAndOne409() throws Exception {
		PRIMARY.delay(800);
		String key = UUID.randomUUID().toString();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			results.add(pool.submit((Callable<Integer>) () -> {
				start.await();
				return verify(key).andReturn().getResponse().getStatus();
			}));
		}
		start.countDown();
		List<Integer> statuses = new ArrayList<>();
		for (Future<Integer> result : results) {
			statuses.add(result.get());
		}
		pool.shutdown();

		assertThat(statuses).containsExactlyInAnyOrder(200, 409);
		assertThat(PRIMARY.hits()).isEqualTo(1);
	}

	private org.springframework.test.web.servlet.ResultActions verify(String key) throws Exception {
		return mvc.perform(post("/v1/verify")
				.header("Idempotency-Key", key)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"customer\":\"42\"}"));
	}

}
