package com.paragon.domain.interfaces;

import com.paragon.domain.models.entities.AuditTrailEntry;

public interface AuditTrailWriteRepo {
    void create(AuditTrailEntry auditTrailEntry);
}
