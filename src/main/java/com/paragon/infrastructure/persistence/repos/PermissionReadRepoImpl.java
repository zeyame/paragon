package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.PermissionReadRepo;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.infrastructure.persistence.daos.PermissionDao;
import com.paragon.infrastructure.persistence.jdbc.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PermissionReadRepoImpl implements PermissionReadRepo {
    private final ReadJdbcHelper jdbcHelper;

    public PermissionReadRepoImpl(ReadJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public Optional<Permission> getByCode(PermissionCode code) {
        String sql = "SELECT * from permissions WHERE code = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("code", code.getValue());

        Optional<PermissionDao> optional = jdbcHelper.queryFirstOrDefault(sql, params, PermissionDao.class);
        return optional.map(PermissionDao::toPermission);
    }
}
