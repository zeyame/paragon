package com.paragon.application.queries.repositoryinterfaces;

import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;

import java.util.Optional;

public interface StaffAccountRequestReadRepo {
    Optional<StaffAccountRequestReadModel> getPendingRequestBySubmitterAndType(StaffAccountId submitter, StaffAccountRequestType requestType);
}
