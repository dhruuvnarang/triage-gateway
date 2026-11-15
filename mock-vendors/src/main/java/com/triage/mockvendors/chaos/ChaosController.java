package com.triage.mockvendors.chaos;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChaosController {

	private final ChaosStore store;

	public ChaosController(ChaosStore store) {
		this.store = store;
	}

	@PutMapping("/chaos/{vendor}")
	public ChaosSettings set(@PathVariable String vendor, @RequestBody ChaosSettings settings) {
		store.set(vendor, settings);
		return settings;
	}

}
