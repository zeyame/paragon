package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import org.springframework.stereotype.Repository;

@Repository
public class AuditTrailWriteRepoImpl implements AuditTrailWriteRepo {
    @Override
    public void create(AuditTrailEntry auditTrailEntry) {
    }
}
