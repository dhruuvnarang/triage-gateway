package com.triage.gateway.vendor;

import com.fasterxml.jackson.databind.JsonNode;
import com.triage.gateway.normalize.VendorResponseNormalizer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

/** Vendor B, used as fallback. Settings live under resilience4j.*.instances.secondary. */
@Component
public class SecondaryVendorClient extends HttpVendorClient {

	public SecondaryVendorClient(RestClient.Builder builder, VendorResponseNormalizer normalizer,
			@Value("${triage.vendors.secondary.url}") String url) {
		super("secondary", builder, url, normalizer::fromVendorB);
	}

	@Override
	@Retry(name = "secondary")
	@CircuitBreaker(name = "secondary")
	@TimeLimiter(name = "secondary")
	public CompletableFuture<String> verify(JsonNode request) {
		return post(request);
	}

}
