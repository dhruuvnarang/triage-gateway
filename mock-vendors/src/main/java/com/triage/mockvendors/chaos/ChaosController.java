package com.triage.mockvendors.chaos;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/chaos")
public class ChaosController {

	private final ChaosService chaos;

	public ChaosController(ChaosService chaos) {
		this.chaos = chaos;
	}

	@GetMapping
	public Map<String, ChaosConfig> all() {
		Map<String, ChaosConfig> result = new LinkedHashMap<>();
		for (Vendor vendor : Vendor.values()) {
			result.put(vendor.id(), chaos.get(vendor));
		}
		return result;
	}

	@GetMapping("/{vendor}")
	public ChaosConfig get(@PathVariable String vendor) {
		return chaos.get(Vendor.fromId(vendor));
	}

	@PutMapping("/{vendor}")
	public ChaosConfig set(@PathVariable String vendor, @RequestBody ChaosConfig config) {
		Vendor target = Vendor.fromId(vendor);
		chaos.set(target, config);
		return chaos.get(target);
	}

	@DeleteMapping("/{vendor}")
	public ResponseEntity<Void> reset(@PathVariable String vendor) {
		chaos.reset(Vendor.fromId(vendor));
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Map<String, String>> invalidConfig(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("error", e.getMessage()));
	}
}
