package com.paragon.application.queries.getstaffaccountbyusername;

import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetStaffAccountByUsernameQueryHandler implements QueryHandler<GetStaffAccountByUsernameQuery, GetStaffAccountByUsernameQueryResponse> {
    private final StaffAccountReadRepo staffAccountReadRepo;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(GetStaffAccountByUsernameQueryHandler.class);

    public GetStaffAccountByUsernameQueryHandler(StaffAccountReadRepo staffAccountReadRepo, AppExceptionHandler appExceptionHandler) {
        this.staffAccountReadRepo = staffAccountReadRepo;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public GetStaffAccountByUsernameQueryResponse handle(GetStaffAccountByUsernameQuery query) {
        try {
            Optional<StaffAccountSummaryReadModel> optionalReadModel = staffAccountReadRepo.findSummaryByUsername(query.username());
            return new GetStaffAccountByUsernameQueryResponse(
                    optionalReadModel.map(StaffAccountSummary::fromReadModel)
            );
        }
        catch (InfraException ex) {
            log.error("Failed to fetch staff account with username '{}' due to infrastructure error.", query.username(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
