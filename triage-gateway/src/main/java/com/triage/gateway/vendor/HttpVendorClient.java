package com.triage.gateway.vendor;

import com.fasterxml.jackson.databind.JsonNode;
import com.triage.gateway.error.VendorException;
import com.triage.gateway.error.VendorRejectedException;
import com.triage.gateway.error.VendorUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Shared HTTP plumbing. Subclasses only pick the name, URL and payload mapping.
 * Resilience4j annotations go on verify() in the subclass.
 */
public abstract class HttpVendorClient implements VendorClient {

	private static final Logger log = LoggerFactory.getLogger(HttpVendorClient.class);
	private static final ExecutorService POOL = Executors.newVirtualThreadPerTaskExecutor();

	private final String name;
	private final RestClient rest;
	private final String url;
	private final Function<JsonNode, String> normalizer;

	protected HttpVendorClient(String name, RestClient.Builder builder, String url,
			Function<JsonNode, String> normalizer) {
		this.name = name;
		this.rest = builder.build();
		this.url = url;
		this.normalizer = normalizer;
	}

	@Override
	public String name() {
		return name;
	}

	protected CompletableFuture<String> post(JsonNode request) {
		return CompletableFuture.supplyAsync(() -> send(request), POOL);
	}

	private String send(JsonNode request) {
		long start = System.nanoTime();
		String outcome = "error";
		try {
			JsonNode payload = rest.post()
					.uri(url)
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(JsonNode.class);
			String status = normalizer.apply(payload);
			outcome = status;
			return status;
		}
		catch (RestClientResponseException e) {
			int code = e.getStatusCode().value();
			outcome = "http-" + code;
			if (code == 503) {
				throw new VendorUnavailableException(name + " returned 503");
			}
			if (code >= 400 && code < 500) {
				throw new VendorRejectedException(name + " rejected request with " + code);
			}
			throw new VendorException(name + " returned " + code);
		}
		catch (RestClientException e) {
			outcome = "unreachable";
			throw new VendorException(name + " unreachable: " + e.getMessage(), e);
		}
		finally {
			log.info("vendor={} latencyMs={} outcome={}", name, (System.nanoTime() - start) / 1_000_000, outcome);
		}
	}

}
