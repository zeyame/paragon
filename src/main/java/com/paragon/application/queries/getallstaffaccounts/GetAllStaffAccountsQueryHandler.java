package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.Username;
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
            DateTimeUtc createdBefore = query.createdBefore() != null ? DateTimeUtc.from(query.createdBefore()) : null;
            DateTimeUtc createdAfter = query.createdAfter() != null ? DateTimeUtc.from(query.createdAfter()) : null;
            validateCreatedDateRange(createdBefore, createdAfter);

            throwIfEnabledByAndDisabledByAreGiven(query.enabledBy(), query.disabledBy());

            Username enabledByUsername = query.enabledBy() != null ? Username.of(query.enabledBy()) : null;
            Username disabledByUsername = query.disabledBy() != null ? Username.of(query.disabledBy()) : null;

            List<StaffAccountSummaryReadModel> staffAccountSummaryReadModels = staffAccountReadRepo.findAllSummaries(
                    StaffAccountStatus.fromString(query.status()),
                    enabledByUsername,
                    disabledByUsername,
                    createdBefore,
                    createdAfter
            );
            List<StaffAccountSummary> staffAccountSummaries = staffAccountSummaryReadModels
                    .stream()
                    .map(StaffAccountSummary::fromReadModel)
                    .toList();
            return new GetAllStaffAccountsQueryResponse(staffAccountSummaries);
        }
        catch (DomainException ex) {
            log.error("GetAllStaffAccounts query failed: domain violation occurred - {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        }
        catch (InfraException ex) {
            log.error("GetAllStaffAccounts query failed: infrastructure error occurred - {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private static void validateCreatedDateRange(DateTimeUtc createdBefore, DateTimeUtc createdAfter) {
        if (createdBefore != null && createdAfter != null && createdBefore.isBefore(createdAfter)) {
            throw new AppException(AppExceptionInfo.invalidStaffAccountCreatedDateRange(
                    createdBefore.getValue().toString(),
                    createdAfter.getValue().toString()
            ));
        }
    }

    private void throwIfEnabledByAndDisabledByAreGiven(String enabledBy, String disabledBy) {
        if (hasText(enabledBy) && hasText(disabledBy)) {
            throw new AppException(AppExceptionInfo.mutuallyExclusiveStaffAccountFilters());
        }
    }

    private Username parseUsername(String usernameValue) {
        if (!hasText(usernameValue)) {
            return null;
        }
        try {
            return Username.of(usernameValue);
        } catch (DomainException ex) {
            return null;
        }
    }

    private DateTimeUtc parseDateTimeUtc(String value) {
        if (!hasText(value)) {
            return null;
        }
        return DateTimeUtc.from(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
