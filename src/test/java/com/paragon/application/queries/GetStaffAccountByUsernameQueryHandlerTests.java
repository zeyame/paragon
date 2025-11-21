package com.paragon.application.queries;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQuery;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQueryHandler;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQueryResponse;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.fixtures.StaffAccountSummaryReadModelFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GetStaffAccountByUsernameQueryHandlerTests {
    private final GetStaffAccountByUsernameQueryHandler sut;
    private final StaffAccountReadRepo staffAccountReadRepoMock;
    private final AppExceptionHandler appExceptionHandlerMock;

    public GetStaffAccountByUsernameQueryHandlerTests() {
        staffAccountReadRepoMock = mock(StaffAccountReadRepo.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        sut = new GetStaffAccountByUsernameQueryHandler(staffAccountReadRepoMock, appExceptionHandlerMock);
    }

    @Test
    void shouldReturnExpectedQueryResponse() {
        // Given
        StaffAccountSummaryReadModel readModel = new StaffAccountSummaryReadModelFixture()
                .withUsername("john_doe")
                .build();
        when(staffAccountReadRepoMock.findByUsername(anyString()))
                .thenReturn(Optional.of(readModel));

        // When
        GetStaffAccountByUsernameQueryResponse queryResponse = sut.handle(new GetStaffAccountByUsernameQuery("john_doe"));

        // Then
        Optional<StaffAccountSummary> optionalStaffAccountSummary = queryResponse.staffAccountSummary();
        assertThat(optionalStaffAccountSummary).isPresent();

        StaffAccountSummary summary = optionalStaffAccountSummary.get();
        assertThat(summary.id()).isEqualTo(readModel.id());
        assertThat(summary.username()).isEqualTo(readModel.username());
        assertThat(summary.status()).isEqualTo(readModel.status());
        assertThat(summary.orderAccessDuration()).isEqualTo(readModel.orderAccessDuration());
        assertThat(summary.modmailTranscriptAccessDuration()).isEqualTo(readModel.modmailTranscriptAccessDuration());
        assertThat(summary.createdAtUtc()).isEqualTo(readModel.createdAtUtc());
    }

    @Test
    void shouldPassCorrectUsernameToRepo() {
        // When
        sut.handle(new GetStaffAccountByUsernameQuery("john_doe"));

        // Then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(staffAccountReadRepoMock, times(1))
                .findByUsername(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo("john_doe");
    }

    @Test
    void shouldCatchInfraException_andTranslateToAppException() {
        // Given
        doThrow(InfraException.class)
                .when(staffAccountReadRepoMock)
                .findByUsername(anyString());

        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(new GetStaffAccountByUsernameQuery("john_doe")))
                .isInstanceOf(AppException.class);
    }
}
