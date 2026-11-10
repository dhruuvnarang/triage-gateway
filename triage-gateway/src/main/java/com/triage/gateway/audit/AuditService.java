package com.triage.gateway.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService {

	private final VendorCallAuditRepository repository;

	public AuditService(VendorCallAuditRepository repository) {
		this.repository = repository;
	}

	public void record(VendorCallRecord record) {
		repository.save(new VendorCallAudit(record));
	}
}
