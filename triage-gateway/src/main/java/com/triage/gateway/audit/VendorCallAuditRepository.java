package com.triage.gateway.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorCallAuditRepository extends JpaRepository<VendorCallAudit, Long> {
}
