package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StaffAccountWriteRepoImpl implements StaffAccountWriteRepo {
    @Override
    public boolean create(StaffAccount staffAccount) {
      return false;
    }

    @Override
    public Optional<StaffAccount> getById(StaffAccountId staffAccountId) {
        return Optional.empty();
    }
}
