package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
}
