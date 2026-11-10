package com.triage.gateway.verification;

import com.triage.gateway.audit.AuditOutcome;
import com.triage.gateway.audit.AuditService;
import com.triage.gateway.audit.VendorCallRecord;
import com.triage.gateway.model.VerificationRequest;
import com.triage.gateway.model.VerificationResponse;
import com.triage.gateway.model.VerificationStatus;
import com.triage.gateway.model.VerificationType;
import com.triage.gateway.vendor.VendorAdapter;
import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.vendor.VendorRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationServiceTest {

	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
	private static final VerificationRequest REQUEST = new VerificationRequest(null, "ABCDE1234F");

	private final VendorRegistry registry = mock(VendorRegistry.class);
	private final AuditService auditService = mock(AuditService.class);
	private final VerificationService service = new VerificationService(
			registry, auditService, Clock.fixed(NOW, ZoneOffset.UTC));
	private final VendorAdapter adapter = mock(VendorAdapter.class);

	@Test
	void returnsAdapterResponseAndAuditsSuccess() {
		VerificationResponse expected = new VerificationResponse(
				"pan-a", VerificationType.PAN, VerificationStatus.VERIFIED, Map.of());
		when(registry.candidates(VerificationType.PAN)).thenReturn(List.of(adapter));
		when(adapter.vendorId()).thenReturn("pan-a");
		when(adapter.verify(REQUEST)).thenReturn(expected);

		VerificationResponse actual = service.verify(VerificationType.PAN, REQUEST, "key-1");

		assertEquals(expected, actual);
		VendorCallRecord record = captureRecord();
		assertEquals(AuditOutcome.SUCCESS, record.outcome());
		assertEquals("key-1", record.idempotencyKey());
		assertEquals("pan-a", record.vendorId());
		assertEquals(NOW, record.occurredAt());
	}

	@Test
	void vendorFailureIsAuditedAndRethrown() {
		when(registry.candidates(VerificationType.PAN)).thenReturn(List.of(adapter));
		when(adapter.vendorId()).thenReturn("pan-a");
		when(adapter.verify(REQUEST)).thenThrow(new VendorCallException("pan-a", "boom", null));

		assertThrows(VendorCallException.class,
				() -> service.verify(VerificationType.PAN, REQUEST, "key-2"));

		VendorCallRecord record = captureRecord();
		assertEquals(AuditOutcome.FAILURE, record.outcome());
		assertEquals("boom", record.errorDetail());
	}

	@Test
	void noConfiguredVendorThrowsWithoutAuditing() {
		when(registry.candidates(VerificationType.BUREAU)).thenReturn(List.of());

		assertThrows(UnsupportedVerificationException.class,
				() -> service.verify(VerificationType.BUREAU, REQUEST, "key-3"));

		verify(auditService, never()).record(any());
	}

	private VendorCallRecord captureRecord() {
		ArgumentCaptor<VendorCallRecord> captor = ArgumentCaptor.forClass(VendorCallRecord.class);
		verify(auditService).record(captor.capture());
		return captor.getValue();
	}
}
