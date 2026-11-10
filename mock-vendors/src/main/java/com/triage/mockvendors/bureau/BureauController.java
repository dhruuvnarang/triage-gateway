package com.triage.mockvendors.bureau;

import com.triage.mockvendors.chaos.ChaosService;
import com.triage.mockvendors.chaos.Fault;
import com.triage.mockvendors.chaos.FaultResponses;
import com.triage.mockvendors.chaos.Vendor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Mock credit bureau. The score is derived from the applicant ID so the same
 * applicant always gets the same score, which keeps idempotency tests stable.
 */
@RestController
@RequestMapping("/mock/bureau")
public class BureauController {

	private final ChaosService chaos;

	public BureauController(ChaosService chaos) {
		this.chaos = chaos;
	}

	@PostMapping("/score")
	public ResponseEntity<?> score(@RequestBody ScoreRequest request) {
		Fault fault = chaos.inject(Vendor.BUREAU);
		if (fault != Fault.NONE) {
			return FaultResponses.of(fault);
		}
		String applicantId = Objects.requireNonNullElse(request.applicantId(), "");
		int score = 300 + Math.floorMod(applicantId.hashCode(), 601);
		return ResponseEntity.ok(new ScoreResponse(request.applicantId(), score, band(score)));
	}

	private static String band(int score) {
		if (score >= 750) {
			return "EXCELLENT";
		}
		if (score >= 650) {
			return "GOOD";
		}
		if (score >= 550) {
			return "FAIR";
		}
		return "POOR";
	}

	public record ScoreRequest(String applicantId, String pan) {
	}

	public record ScoreResponse(String applicantId, int score, String band) {
	}
}
