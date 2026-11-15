package com.triage.mockvendors.vendor;

import com.triage.mockvendors.chaos.ChaosSettings;
import com.triage.mockvendors.chaos.ChaosStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Two fake verification APIs with different payload shapes, both with chaos applied. */
@RestController
public class MockVendorController {

	private final ChaosStore chaos;

	public MockVendorController(ChaosStore chaos) {
		this.chaos = chaos;
	}

	@PostMapping("/mock/{vendor}/verify")
	public ResponseEntity<Map<String, Object>> verify(@PathVariable String vendor) throws InterruptedException {
		ChaosSettings settings = chaos.get(vendor);
		if (settings.forceTimeout()) {
			Thread.sleep(30_000);
		}
		if (settings.latencyMs() > 0) {
			Thread.sleep(settings.latencyMs());
		}
		if (ThreadLocalRandom.current().nextInt(100) < settings.errorPercent()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "chaos"));
		}
		return switch (vendor) {
			case "vendor-a" -> ResponseEntity.ok(Map.of(
					"verdict", "VALID",
					"reference", "A-" + UUID.randomUUID()));
			case "vendor-b" -> ResponseEntity.ok(Map.of(
					"result", Map.of("code", "OK"),
					"ref", "B-" + UUID.randomUUID()));
			default -> ResponseEntity.notFound().build();
		};
	}

}
