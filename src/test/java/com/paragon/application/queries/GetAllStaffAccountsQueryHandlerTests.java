package com.paragon.application.queries;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.context.ActorContext;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class GetAllStaffAccountsQueryHandlerTests {
    private final GetAllStaffAccountsQueryHandler sut;
    private final StaffAccountReadRepo staffAccountReadRepoMock;
    private final ActorContext actorContextMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final GetAllStaffAccountsQuery query;
    private final String requestingStaffAccountId;

    public GetAllStaffAccountsQueryHandlerTests() {
        staffAccountReadRepoMock = mock(StaffAccountReadRepo.class);
        actorContextMock = mock(ActorContext.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new GetAllStaffAccountsQueryHandler(staffAccountReadRepoMock, actorContextMock, appExceptionHandlerMock);

        query = new GetAllStaffAccountsQuery();

        requestingStaffAccountId = UUID.randomUUID().toString();
        when(actorContextMock.getActorId()).thenReturn(requestingStaffAccountId);

        when(staffAccountReadRepoMock.exists(StaffAccountId.from(requestingStaffAccountId)))
                .thenReturn(true);
        when(staffAccountReadRepoMock.hasPermission(StaffAccountId.from(requestingStaffAccountId), SystemPermissions.VIEW_ACCOUNTS_LIST))
                .thenReturn(true);
    }

    @Test
    void givenValidQuery_shouldReturnAllStaffAccountSummaries() {
        // Given
        List<StaffAccountSummaryReadModel> readModels = List.of(
                new StaffAccountSummaryReadModel(
                        UUID.randomUUID(),
                        "john_doe",
                        "active",
                        10,
                        5,
                        Instant.now()
                ),
                new StaffAccountSummaryReadModel(
                        UUID.randomUUID(),
                        "jane_smith",
                        "pending_password_change",
                        14,
                        7,
                        Instant.now()
                )
        );
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(readModels);

        // When
        GetAllStaffAccountsQueryResponse response = sut.handle(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.staffAccountSummaries().size()).isEqualTo(2);

        List<StaffAccountSummary> summaries = response.staffAccountSummaries();
        assertThat(summaries.get(0).username()).isEqualTo("john_doe");
        assertThat(summaries.get(0).status()).isEqualTo("active");
        assertThat(summaries.get(1).username()).isEqualTo("jane_smith");
        assertThat(summaries.get(1).status()).isEqualTo("pending_password_change");
    }

    @Test
    void givenValidQuery_shouldCorrectlyMapReadModelsToSummaries() {
        // Given
        UUID expectedId = UUID.randomUUID();
        Instant expectedTimestamp = Instant.now();
        StaffAccountSummaryReadModel readModel = new StaffAccountSummaryReadModel(
                expectedId,
                "test_user",
                "active",
                15,
                10,
                expectedTimestamp
        );
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(List.of(readModel));

        // When
        GetAllStaffAccountsQueryResponse response = sut.handle(query);

        // Then
        assertThat(response.staffAccountSummaries().size()).isEqualTo(1);

        StaffAccountSummary summary = response.staffAccountSummaries().get(0);
        assertThat(summary.id()).isEqualTo(expectedId);
        assertThat(summary.username()).isEqualTo("test_user");
        assertThat(summary.status()).isEqualTo("active");
        assertThat(summary.orderAccessDuration()).isEqualTo(15);
        assertThat(summary.modmailTranscriptAccessDuration()).isEqualTo(10);
        assertThat(summary.createdAtUtc()).isEqualTo(expectedTimestamp);
    }

    @Test
    void givenValidQuery_shouldCallRepoToFindAllSummaries() {
        // Given
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(List.of());

        // When
        sut.handle(query);

        // Then
        verify(staffAccountReadRepoMock, times(1)).findAllSummaries();
    }

    @Test
    void givenValidQuery_shouldReturnEmptyListWhenNoStaffAccountsExist() {
        // Given
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(List.of());

        // When
        GetAllStaffAccountsQueryResponse response = sut.handle(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.staffAccountSummaries().size()).isEqualTo(0);
    }

    @Test
    void givenValidQuery_shouldCheckIfRequestingStaffAccountExists() {
        // Given
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(List.of());

        // When
        sut.handle(query);

        // Then
        verify(staffAccountReadRepoMock, times(1)).exists(StaffAccountId.from(requestingStaffAccountId));
    }

    @Test
    void givenValidQuery_shouldCheckIfRequestingStaffAccountHasRequiredPermission() {
        // Given
        when(staffAccountReadRepoMock.findAllSummaries()).thenReturn(List.of());

        // When
        sut.handle(query);

        // Then
        verify(staffAccountReadRepoMock, times(1))
                .hasPermission(StaffAccountId.from(requestingStaffAccountId), SystemPermissions.VIEW_ACCOUNTS_LIST);
    }

    @Test
    void whenRequestingStaffAccountDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountReadRepoMock.exists(StaffAccountId.from(requestingStaffAccountId)))
                .thenReturn(false);

        AppException expectedAppException = new AppException(AppExceptionInfo.staffAccountNotFound(requestingStaffAccountId));

        // When & Then
        assertThatThrownBy(() -> sut.handle(query))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenRequestingStaffAccountLacksRequiredPermission_shouldThrowAppException() {
        // Given
        when(staffAccountReadRepoMock.hasPermission(StaffAccountId.from(requestingStaffAccountId), SystemPermissions.VIEW_ACCOUNTS_LIST))
                .thenReturn(false);

        AppException expectedAppException = new AppException(AppExceptionInfo.permissionAccessDenied("view all registered staff accounts"));

        // When & Then
        assertThatThrownBy(() -> sut.handle(query))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenDomainExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        DomainException domainException = mock(DomainException.class);
        when(staffAccountReadRepoMock.exists(any(StaffAccountId.class)))
                .thenThrow(domainException);

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(query))
                .isInstanceOf(AppException.class);

        verify(appExceptionHandlerMock, times(1)).handleDomainException(domainException);
    }

    @Test
    void whenInfraExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        InfraException infraException = mock(InfraException.class);
        when(staffAccountReadRepoMock.findAllSummaries())
                .thenThrow(infraException);

        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(query))
                .isInstanceOf(AppException.class);

        verify(appExceptionHandlerMock, times(1)).handleInfraException(infraException);
    }
}