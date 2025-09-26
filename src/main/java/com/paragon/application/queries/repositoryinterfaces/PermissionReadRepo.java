package com.paragon.application.queries.repositoryinterfaces;

import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;

import java.util.Optional;

public interface PermissionReadRepo {
    Optional<Permission> getByCode(PermissionCode code);
}
