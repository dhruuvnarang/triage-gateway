package com.triage.mockvendors.chaos;

import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public enum Vendor {
	PAN_A("pan-a"),
	PAN_B("pan-b"),
	BUREAU("bureau");

	private final String id;

	Vendor(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static Vendor fromId(String id) {
		return Arrays.stream(values())
				.filter(v -> v.id.equals(id))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown vendor: " + id));
	}
}
