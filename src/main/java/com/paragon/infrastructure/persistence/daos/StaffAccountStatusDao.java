package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.enums.StaffAccountStatus;

public record StaffAccountStatusDao(String status) {
    public StaffAccountStatus toEnum() {
        return StaffAccountStatus.fromString(status);
    }
}
