package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RefreshTokenWriteRepoImpl implements RefreshTokenWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    public RefreshTokenWriteRepoImpl(WriteJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public void create(RefreshToken refreshToken) {}

    @Override
    public List<RefreshToken> getActiveTokensByStaffAccountId(StaffAccountId staffAccountId) {
        return List.of();
    }
}
