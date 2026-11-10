package com.triage.mockvendors.pan;

import com.triage.mockvendors.chaos.ChaosService;
import com.triage.mockvendors.chaos.Fault;
import com.triage.mockvendors.chaos.FaultResponses;
import com.triage.mockvendors.chaos.Vendor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Mock PAN vendor A: flat JSON request and response. */
@RestController
@RequestMapping("/mock/pan-a")
public class PanVendorAController {

	private final ChaosService chaos;

	public PanVendorAController(ChaosService chaos) {
		this.chaos = chaos;
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
		Fault fault = chaos.inject(Vendor.PAN_A);
		if (fault != Fault.NONE) {
			return FaultResponses.of(fault);
		}
		boolean valid = PanRules.isValid(request.pan());
		return ResponseEntity.ok(new VerifyResponse(request.pan(), valid, valid ? "MOCK HOLDER" : null));
	}

	public record VerifyRequest(String pan) {
	}

	public record VerifyResponse(String pan, boolean valid, String name) {
	}
}
