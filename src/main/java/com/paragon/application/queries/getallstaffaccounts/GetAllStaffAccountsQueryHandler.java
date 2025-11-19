package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
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
            Instant createdBefore = parseInstant(query.createdBefore());
            Instant createdAfter = parseInstant(query.createdAfter());
            validateQuery(query, createdBefore, createdAfter);

            Username enabledByUsername = parseUsername(query.enabledBy());
            Username disabledByUsername = parseUsername(query.disabledBy());

            List<StaffAccountSummaryReadModel> staffAccountSummaryReadModels = staffAccountReadRepo.findAll(
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
        catch (InfraException ex) {
            log.error("GetAllStaffAccounts query failed: infrastructure error occurred - {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private void validateQuery(GetAllStaffAccountsQuery query, Instant createdBefore, Instant createdAfter) {
        if (query == null) {
            return;
        }
        validateEnabledByAndDisabledBy(query.enabledBy(), query.disabledBy());
        validateCreatedDateRange(createdBefore, createdAfter);
    }

    private static void validateCreatedDateRange(Instant createdBefore, Instant createdAfter) {
        if (createdBefore != null && createdAfter != null && createdBefore.isBefore(createdAfter)) {
            throw new AppException(AppExceptionInfo.invalidStaffAccountCreatedDateRange(
                    createdBefore.toString(),
                    createdAfter.toString()
            ));
        }
    }

    private void validateEnabledByAndDisabledBy(String enabledBy, String disabledBy) {
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

    private Instant parseInstant(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
