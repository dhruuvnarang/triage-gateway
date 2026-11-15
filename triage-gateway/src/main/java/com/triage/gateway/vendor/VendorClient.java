package com.triage.gateway.vendor;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.CompletableFuture;

/** One third-party verification API. verify() returns the normalized status. */
public interface VendorClient {

	String name();

	CompletableFuture<String> verify(JsonNode request);

}
