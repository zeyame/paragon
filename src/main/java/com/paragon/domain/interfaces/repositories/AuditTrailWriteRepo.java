package com.paragon.domain.interfaces.repositories;

import com.paragon.domain.models.entities.AuditTrailEntry;

public interface AuditTrailWriteRepo {
    void create(AuditTrailEntry auditTrailEntry);
}
