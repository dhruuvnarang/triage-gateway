package com.triage.gateway.support;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Fake vendor over HTTP. Counts calls and answers with a configurable status. */
public final class StubVendor {

	private final HttpServer server;
	private final String okBody;
	private final AtomicInteger hits = new AtomicInteger();
	private volatile int status = 200;
	private volatile long delayMs;

	private StubVendor(HttpServer server, String okBody) {
		this.server = server;
		this.okBody = okBody;
	}

	public static StubVendor start(String okBody) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.setExecutor(Executors.newCachedThreadPool());
			StubVendor vendor = new StubVendor(server, okBody);
			server.createContext("/", exchange -> {
				vendor.hits.incrementAndGet();
				exchange.getRequestBody().readAllBytes();
				sleep(vendor.delayMs);
				byte[] body = (vendor.status == 200 ? vendor.okBody : "{\"error\":\"stub\"}")
						.getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(vendor.status, body.length);
				try (var out = exchange.getResponseBody()) {
					out.write(body);
				}
			});
			server.start();
			return vendor;
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String url() {
		return "http://localhost:" + server.getAddress().getPort() + "/verify";
	}

	public int hits() {
		return hits.get();
	}

	public void reset(int status) {
		this.hits.set(0);
		this.status = status;
	}

	public void delay(long ms) {
		this.delayMs = ms;
	}

	public void stop() {
		server.stop(0);
	}

	private static void sleep(long ms) {
		if (ms <= 0) {
			return;
		}
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
