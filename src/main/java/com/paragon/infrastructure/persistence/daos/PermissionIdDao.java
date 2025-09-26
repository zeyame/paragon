package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.models.valueobjects.PermissionId;

import java.util.UUID;

public record PermissionIdDao(
        UUID id
)
{
    public PermissionId toPermissionId() {
        return PermissionId.of(id);
    }
}
