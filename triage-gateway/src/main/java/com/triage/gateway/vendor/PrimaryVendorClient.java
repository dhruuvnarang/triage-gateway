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

/** Vendor A, tried first. Settings live under resilience4j.*.instances.primary. */
@Component
public class PrimaryVendorClient extends HttpVendorClient {

	public PrimaryVendorClient(RestClient.Builder builder, VendorResponseNormalizer normalizer,
			@Value("${triage.vendors.primary.url}") String url) {
		super("primary", builder, url, normalizer::fromVendorA);
	}

	@Override
	@Retry(name = "primary")
	@CircuitBreaker(name = "primary")
	@TimeLimiter(name = "primary")
	public CompletableFuture<String> verify(JsonNode request) {
		return post(request);
	}

}
