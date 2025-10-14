package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.context.ActorContext;
import com.paragon.application.events.EventBus;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllStaffAccountsQueryHandler implements QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> {
    private final StaffAccountReadRepo staffAccountReadRepo;
    private final ActorContext actorContext;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(GetAllStaffAccountsQueryHandler.class);

    public GetAllStaffAccountsQueryHandler(StaffAccountReadRepo staffAccountReadRepo, ActorContext actorContext, EventBus eventBus, AppExceptionHandler appExceptionHandler) {
        this.staffAccountReadRepo = staffAccountReadRepo;
        this.actorContext = actorContext;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public GetAllStaffAccountsQueryResponse handle(GetAllStaffAccountsQuery query) {
        String requestStaffAccountId = actorContext.getActorId();
        try {
            if (!staffAccountReadRepo.exists(StaffAccountId.from(requestStaffAccountId))) {
                log.error("GetAllStaffAccounts query denied: requestingStaffId='{}' does not exist.", requestStaffAccountId);
                throw new AppException(AppExceptionInfo.staffAccountNotFound(requestStaffAccountId));
            }

            if (!staffAccountReadRepo.hasPermission(StaffAccountId.from(requestStaffAccountId), SystemPermissions.VIEW_ACCOUNTS_LIST)) {
                log.error("GetAllStaffAccounts request denied: requestingStaffId='{}' lacks required permission '{}'.", requestStaffAccountId, SystemPermissions.VIEW_ACCOUNTS_LIST);
                throw new AppException(AppExceptionInfo.permissionAccessDenied("view all registered staff accounts"));
            }

            List<StaffAccountSummaryReadModel> staffAccountSummaryReadModels = staffAccountReadRepo.findAllSummaries();
            List<StaffAccountSummary> staffAccountSummaries = staffAccountSummaryReadModels
                    .stream()
                    .map(StaffAccountSummary::fromReadModel)
                    .toList();
            return new GetAllStaffAccountsQueryResponse(staffAccountSummaries);
        } catch (DomainException ex) {
            log.error(
                    "GetAllStaffAccounts query failed for requestingStaffId={}: domain rule violation - {}",
                    requestStaffAccountId, ex.getMessage(), ex
            );
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error(
                    "GetAllStaffAccounts query failed for requestingStaffId={}: infrastructure error occurred - {}",
                    requestStaffAccountId, ex.getMessage(), ex
            );
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
