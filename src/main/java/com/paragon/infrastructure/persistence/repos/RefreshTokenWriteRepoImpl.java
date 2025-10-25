package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenWriteRepoImpl implements RefreshTokenWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    public RefreshTokenWriteRepoImpl(WriteJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public void create(RefreshToken refreshToken) {}
}
