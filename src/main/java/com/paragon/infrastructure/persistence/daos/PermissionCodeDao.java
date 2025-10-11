package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.models.valueobjects.PermissionCode;

public record PermissionCodeDao(String permissionCode) {
    public PermissionCode toPermissionCode() {
        return PermissionCode.of(permissionCode);
    }
}
