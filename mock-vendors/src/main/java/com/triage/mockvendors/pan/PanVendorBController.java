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

/** Mock PAN vendor B: nested request and response envelope with different field names. */
@RestController
@RequestMapping("/mock/pan-b")
public class PanVendorBController {

	private final ChaosService chaos;

	public PanVendorBController(ChaosService chaos) {
		this.chaos = chaos;
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
		Fault fault = chaos.inject(Vendor.PAN_B);
		if (fault != Fault.NONE) {
			return FaultResponses.of(fault);
		}
		String pan = request.data() == null ? null : request.data().panNumber();
		boolean valid = PanRules.isValid(pan);
		return ResponseEntity.ok(new VerifyResponse("success",
				new VerifyData(valid ? "VALID" : "INVALID", valid ? "MOCK HOLDER" : null)));
	}

	public record VerifyRequest(RequestData data) {
	}

	public record RequestData(String panNumber) {
	}

	public record VerifyResponse(String status, VerifyData data) {
	}

	public record VerifyData(String panStatus, String holderName) {
	}
}
