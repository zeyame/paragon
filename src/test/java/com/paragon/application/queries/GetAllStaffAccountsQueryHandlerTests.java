package com.paragon.application.queries;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GetAllStaffAccountsQueryHandlerTests {
    private final GetAllStaffAccountsQueryHandler sut;
    private final StaffAccountReadRepo staffAccountReadRepoMock;
    private final AppExceptionHandler appExceptionHandlerMock;

    public GetAllStaffAccountsQueryHandlerTests() {
        staffAccountReadRepoMock = mock(StaffAccountReadRepo.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new GetAllStaffAccountsQueryHandler(staffAccountReadRepoMock, appExceptionHandlerMock);
}

    @Test
    void givenQueryWithNoFilters_shouldReturnAllStaffAccountSummaries() {
        // Given
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery(null, null, null, null, null);

        List<StaffAccountSummaryReadModel> readModels = List.of(
                new StaffAccountSummaryReadModel(
                        UUID.randomUUID(),
                        "john_doe",
                        "active",
                        Instant.now()
                ),
                new StaffAccountSummaryReadModel(
                        UUID.randomUUID(),
                        "jane_smith",
                        "pending_password_change",
                        Instant.now()
                )
        );
        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any())).thenReturn(readModels);

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
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery("ACTIVE", null, null, null, null);

        UUID expectedId = UUID.randomUUID();
        Instant expectedTimestamp = Instant.now();
        StaffAccountSummaryReadModel readModel = new StaffAccountSummaryReadModel(
                expectedId,
                "test_user",
                "active",
                expectedTimestamp
        );
        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any())).thenReturn(List.of(readModel));

        // When
        GetAllStaffAccountsQueryResponse response = sut.handle(query);

        // Then
        assertThat(response.staffAccountSummaries().size()).isEqualTo(1);

        StaffAccountSummary summary = response.staffAccountSummaries().getFirst();
        assertThat(summary.id()).isEqualTo(expectedId);
        assertThat(summary.username()).isEqualTo("test_user");
        assertThat(summary.status()).isEqualTo("active");
        assertThat(summary.createdAtUtc()).isEqualTo(expectedTimestamp);
    }

    @Test
    void givenValidQuery_shouldCallRepoOnceToFindAllSummaries() {
        // Given
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery("ACTIVE", null, null, null, null);

        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any())).thenReturn(List.of());

        // When
        sut.handle(query);

        // Then
        verify(staffAccountReadRepoMock, times(1)).findAllSummaries(any(), any(), any(), any(), any());
    }

    @Test
    void givenValidQuery_shouldReturnEmptyListWhenNoStaffAccountsExist() {
        // Given
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery("ACTIVE", null, null, null, null);

        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any())).thenReturn(List.of());

        // When
        GetAllStaffAccountsQueryResponse response = sut.handle(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.staffAccountSummaries()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideValidFilterCombinations")
    void shouldPassFiltersCorrectlyToRepository(
            String status,
            String enabledBy,
            String disabledBy,
            String createdBefore,
            String createdAfter,
            StaffAccountStatus expectedStatus,
            Username expectedEnabledByUsername,
            Username expectedDisabledByUsername,
            DateTimeUtc expectedCreatedBefore,
            DateTimeUtc expectedCreatedAfter
    ) {
        // Given
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery(
                status,
                enabledBy,
                disabledBy,
                createdBefore,
                createdAfter
        );

        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        ArgumentCaptor<StaffAccountStatus> statusCaptor = ArgumentCaptor.forClass(StaffAccountStatus.class);
        ArgumentCaptor<Username> enabledByCaptor = ArgumentCaptor.forClass(Username.class);
        ArgumentCaptor<Username> disabledByCaptor = ArgumentCaptor.forClass(Username.class);
        ArgumentCaptor<DateTimeUtc> createdBeforeCaptor = ArgumentCaptor.forClass(DateTimeUtc.class);
        ArgumentCaptor<DateTimeUtc> createdAfterCaptor = ArgumentCaptor.forClass(DateTimeUtc.class);

        // When
        sut.handle(query);

        // Then
        verify(staffAccountReadRepoMock, times(1)).findAllSummaries(
                statusCaptor.capture(),
                enabledByCaptor.capture(),
                disabledByCaptor.capture(),
                createdBeforeCaptor.capture(),
                createdAfterCaptor.capture()
        );

        assertThat(statusCaptor.getValue()).isEqualTo(expectedStatus);
        assertThat(enabledByCaptor.getValue()).isEqualTo(expectedEnabledByUsername);
        assertThat(disabledByCaptor.getValue()).isEqualTo(expectedDisabledByUsername);
        assertThat(createdBeforeCaptor.getValue()).isEqualTo(expectedCreatedBefore);
        assertThat(createdAfterCaptor.getValue()).isEqualTo(expectedCreatedAfter);
    }

    @Test
    void shouldThrowAppException_whenEnabledAndDisabledFiltersAreProvided() {
        // Given
        GetAllStaffAccountsQuery invalidQuery = new GetAllStaffAccountsQuery(
                null,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                null
        );

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidQuery))
                .extracting("message", "errorCode")
                .containsExactly(AppExceptionInfo.mutuallyExclusiveStaffAccountFilters().getMessage(), AppExceptionInfo.mutuallyExclusiveStaffAccountFilters().getAppErrorCode());
        verify(staffAccountReadRepoMock, never()).findAllSummaries(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowAppException_whenCreatedBeforeIsPriorToCreatedAfter() {
        // Given
        GetAllStaffAccountsQuery invalidQuery = new GetAllStaffAccountsQuery(
                null,
                null,
                null,
                "2024-01-01T00:00:00Z",
                "2024-02-01T00:00:00Z"
        );

        String expectedErrorMessage = AppExceptionInfo.invalidStaffAccountCreatedDateRange(
                invalidQuery.createdBefore(),
                invalidQuery.createdAfter()
        ).getMessage();
        int expectedErrorCode = AppExceptionInfo.invalidStaffAccountCreatedDateRange(
                invalidQuery.createdBefore(),
                invalidQuery.createdAfter()
        ).getAppErrorCode();

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidQuery))
                .extracting("message", "errorCode")
                .containsExactly(expectedErrorMessage, expectedErrorCode);
        verify(staffAccountReadRepoMock, never()).findAllSummaries(any(), any(), any(), any(), any());
    }

    @Test
    void whenInfraExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        GetAllStaffAccountsQuery query = new GetAllStaffAccountsQuery("ACTIVE", null, null, null, null);

        when(staffAccountReadRepoMock.findAllSummaries(any(), any(), any(), any(), any()))
                .thenThrow(mock(InfraException.class));

        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(query))
                .isInstanceOf(AppException.class);
    }

    private static Stream<Arguments> provideValidFilterCombinations() {
        Username enabledByUsername = Username.of("admin_user");
        Username disabledByUsername = Username.of("admin_user");
        DateTimeUtc beforeDateTime = DateTimeUtc.from("2024-12-31T23:59:59Z");
        DateTimeUtc afterDateTime = DateTimeUtc.from("2024-01-01T00:00:00Z");

        return Stream.of(
                // No filters
                Arguments.of(null, null, null, null, null, null, null, null, null, null),

                // Each filter independently
                Arguments.of("ACTIVE", null, null, null, null, StaffAccountStatus.ACTIVE, null, null, null, null),
                Arguments.of(null, "admin_user", null, null, null, null, enabledByUsername, null, null, null),
                Arguments.of(null, null, "admin_user", null, null, null, null, disabledByUsername, null, null),
                Arguments.of(null, null, null, "2024-12-31T23:59:59Z", null, null, null, null, beforeDateTime, null),
                Arguments.of(null, null, null, null, "2024-01-01T00:00:00Z", null, null, null, null, afterDateTime),

                // Representative multi-filter combinations
                Arguments.of("ACTIVE", "admin_user", null, null, null, StaffAccountStatus.ACTIVE, enabledByUsername, null, null, null),
                Arguments.of(null, null, null, "2024-12-31T23:59:59Z", "2024-01-01T00:00:00Z", null, null, null, beforeDateTime, afterDateTime),
                Arguments.of("ACTIVE", null, null, "2024-12-31T23:59:59Z", "2024-01-01T00:00:00Z", StaffAccountStatus.ACTIVE, null, null, beforeDateTime, afterDateTime),
                Arguments.of("DISABLED", null, "admin_user", "2024-12-31T23:59:59Z", "2024-01-01T00:00:00Z", StaffAccountStatus.DISABLED, null, disabledByUsername, beforeDateTime, afterDateTime)
        );
    }
}
