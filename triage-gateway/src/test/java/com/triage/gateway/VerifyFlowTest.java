package com.triage.gateway;

import com.triage.gateway.support.StubVendor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VerifyFlowTest {

	private static final StubVendor PRIMARY = StubVendor.start("{\"verdict\":\"VALID\"}");
	private static final StubVendor SECONDARY = StubVendor.start("{\"result\":{\"code\":\"OK\"}}");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private CircuitBreakerRegistry circuitBreakers;

	@DynamicPropertySource
	static void vendorUrls(DynamicPropertyRegistry registry) {
		registry.add("triage.vendors.primary.url", PRIMARY::url);
		registry.add("triage.vendors.secondary.url", SECONDARY::url);
	}

	@AfterAll
	static void stopVendors() {
		PRIMARY.stop();
		SECONDARY.stop();
	}

	@BeforeEach
	void resetState() {
		PRIMARY.reset(200);
		SECONDARY.reset(200);
		circuitBreakers.circuitBreaker("primary").reset();
		circuitBreakers.circuitBreaker("secondary").reset();
	}

	@Test
	void happyPathAnswersFromPrimary() throws Exception {
		verify(UUID.randomUUID().toString())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("VERIFIED"))
				.andExpect(jsonPath("$.vendor").value("primary"))
				.andExpect(jsonPath("$.fromFallback").value(false));
		assertThat(PRIMARY.hits()).isEqualTo(1);
	}

	@Test
	void missingIdempotencyKeyReturns400() throws Exception {
		mvc.perform(post("/v1/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void circuitOpensAfterRepeatedPrimaryFailures() throws Exception {
		PRIMARY.reset(503);
		for (int i = 0; i < 5; i++) {
			verify(UUID.randomUUID().toString());
		}
		CircuitBreaker primary = circuitBreakers.circuitBreaker("primary");
		assertThat(primary.getState()).isEqualTo(CircuitBreaker.State.OPEN);
	}

	@Test
	void retrySkipsFourHundredErrors() throws Exception {
		PRIMARY.reset(400);
		verify(UUID.randomUUID().toString())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromFallback").value(true));
		assertThat(PRIMARY.hits()).isEqualTo(1);
	}

	@Test
	void fallbackRoutesToSecondaryWhenPrimaryFails() throws Exception {
		PRIMARY.reset(503);
		verify(UUID.randomUUID().toString())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vendor").value("secondary"))
				.andExpect(jsonPath("$.fromFallback").value(true));
		assertThat(PRIMARY.hits()).isEqualTo(3);
	}

	@Test
	void bothVendorsDownReturnsClear503() throws Exception {
		PRIMARY.reset(503);
		SECONDARY.reset(503);
		verify(UUID.randomUUID().toString())
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("ALL_VENDORS_UNAVAILABLE"));
	}

	@Test
	void duplicateKeyCallsVendorExactlyOnceEvenConcurrently() throws Exception {
		String key = UUID.randomUUID().toString();
		int threads = 8;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		for (int i = 0; i < threads; i++) {
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

		assertThat(statuses).allMatch(s -> s == 200 || s == 409);
		assertThat(PRIMARY.hits()).isEqualTo(1);

		verify(key).andExpect(status().isOk());
		assertThat(PRIMARY.hits()).isEqualTo(1);
	}

	@Test
	void unknownPathReturns404() throws Exception {
		mvc.perform(post("/v1/call/pan")
						.header("Idempotency-Key", UUID.randomUUID().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isNotFound());
	}

	private ResultActions verify(String key) throws Exception {
		return mvc.perform(post("/v1/verify")
				.header("Idempotency-Key", key)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"customer\":\"42\"}"));
	}

	/** In-memory stand-in for Redis so the tests need no server. */
	@TestConfiguration
	static class InMemoryRedis {

		@Bean
		StringRedisTemplate stringRedisTemplate() {
			Map<String, String> store = new ConcurrentHashMap<>();
			@SuppressWarnings("unchecked")
			ValueOperations<String, String> ops = mock(ValueOperations.class);
			StringRedisTemplate template = mock(StringRedisTemplate.class);
			when(template.opsForValue()).thenReturn(ops);
			when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class)))
					.thenAnswer(i -> store.putIfAbsent(i.getArgument(0), i.getArgument(1)) == null);
			when(ops.get(anyString())).thenAnswer(i -> store.get(i.getArgument(0)));
			doAnswer(i -> {
				store.put(i.getArgument(0), i.getArgument(1));
				return null;
			}).when(ops).set(anyString(), anyString(), any(Duration.class));
			when(template.delete(anyString())).thenAnswer(i -> store.remove(i.getArgument(0)) != null);
			return template;
		}

	}

}
