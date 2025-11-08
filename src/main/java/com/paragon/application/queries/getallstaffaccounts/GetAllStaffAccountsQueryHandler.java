package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GetAllStaffAccountsQueryHandler implements QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> {
    private final StaffAccountReadRepo staffAccountReadRepo;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(GetAllStaffAccountsQueryHandler.class);

    public GetAllStaffAccountsQueryHandler(StaffAccountReadRepo staffAccountReadRepo, AppExceptionHandler appExceptionHandler) {
        this.staffAccountReadRepo = staffAccountReadRepo;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public GetAllStaffAccountsQueryResponse handle(GetAllStaffAccountsQuery query) {
        try {
            validateQuery(query);
            List<StaffAccountSummaryReadModel> staffAccountSummaryReadModels = staffAccountReadRepo.findAllSummaries();
            List<StaffAccountSummary> staffAccountSummaries = staffAccountSummaryReadModels
                    .stream()
                    .map(StaffAccountSummary::fromReadModel)
                    .toList();
            return new GetAllStaffAccountsQueryResponse(staffAccountSummaries);
        } catch (InfraException ex) {
            log.error("GetAllStaffAccounts query failed: infrastructure error occurred - {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private void validateQuery(GetAllStaffAccountsQuery query) {
        if (query.enabledBy() != null && query.disabledBy() != null) {
            throw new AppException(AppExceptionInfo.mutuallyExclusiveStaffAccountFilters());
        }

        if (query.createdBefore() != null && query.createdAfter() != null) {
            Instant createdBefore = Instant.parse(query.createdBefore());
            Instant createdAfter = Instant.parse(query.createdAfter());
            if (createdBefore.isBefore(createdAfter)) {
                throw new AppException(AppExceptionInfo.invalidStaffAccountCreatedDateRange(query.createdBefore(), query.createdAfter()));
            }
        }
    }
}
