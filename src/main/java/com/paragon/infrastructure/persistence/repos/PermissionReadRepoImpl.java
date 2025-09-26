package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.PermissionReadRepo;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PermissionReadRepoImpl implements PermissionReadRepo {
    @Override
    public Optional<Permission> getByCode(PermissionCode code) {
        return Optional.empty();
    }
}
