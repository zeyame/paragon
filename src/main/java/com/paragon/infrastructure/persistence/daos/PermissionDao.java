package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.enums.PermissionCategory;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.PermissionId;

import java.util.UUID;

public record PermissionDao(
        UUID id,
        String code,
        String category,
        String description
) {
    public Permission toPermission() {
        return Permission.createFrom(
                PermissionId.of(id),
                PermissionCode.of(code),
                PermissionCategory.valueOf(category),
                description
        );
    }
}
