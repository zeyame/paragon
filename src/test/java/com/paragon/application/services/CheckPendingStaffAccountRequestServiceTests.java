package com.paragon.application.services;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.CheckPendingStaffAccountRequestService;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountRequestReadRepo;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountRequestReadModelFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class CheckPendingStaffAccountRequestServiceTests {
    @Nested
    class HasPendingRequest {
        private final CheckPendingStaffAccountRequestService sut;
        private final StaffAccountRequestReadRepo staffAccountRequestReadRepoMock;
        private final AppExceptionHandler appExceptionHandlerMock;

        public HasPendingRequest() {
            staffAccountRequestReadRepoMock = mock(StaffAccountRequestReadRepo.class);
            appExceptionHandlerMock = mock(AppExceptionHandler.class);
            sut = new CheckPendingStaffAccountRequestServiceImpl(staffAccountRequestReadRepoMock, appExceptionHandlerMock);
        }

        @Test
        void shouldCallRepoMethodWithCorrectArguments() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            StaffAccountRequestType requestType = StaffAccountRequestType.PASSWORD_CHANGE;

            // When
            sut.hasPendingRequest(staffAccountId, requestType);

            // Then
            ArgumentCaptor<StaffAccountId> staffAccountIdCaptor = ArgumentCaptor.forClass(StaffAccountId.class);
            ArgumentCaptor<StaffAccountRequestType> staffAccountRequestTypeCaptor = ArgumentCaptor.forClass(StaffAccountRequestType.class);
            verify(staffAccountRequestReadRepoMock, times(1)).getPendingRequestBySubmitterAndType(
                    staffAccountIdCaptor.capture(),
                    staffAccountRequestTypeCaptor.capture()
            );
            assertThat(staffAccountIdCaptor.getValue()).isEqualTo(staffAccountId);
            assertThat(staffAccountRequestTypeCaptor.getValue()).isEqualTo(requestType);
        }

        @Test
        void shouldReturnTrue_whenPendingRequestExists() {
            // Given
            when(staffAccountRequestReadRepoMock.getPendingRequestBySubmitterAndType(any(StaffAccountId.class), any(StaffAccountRequestType.class)))
                    .thenReturn(Optional.of(StaffAccountRequestReadModelFixture.validPendingRequest()));

            // When
            boolean hasPendingRequest = sut.hasPendingRequest(StaffAccountId.generate(), StaffAccountRequestType.CENSORED_ORDER_CONTENT);

            // Then
            assertThat(hasPendingRequest).isTrue();
        }

        @Test
        void shouldReturnFalse_whenPendingRequestDoesNotExist() {
            // Given
            when(staffAccountRequestReadRepoMock.getPendingRequestBySubmitterAndType(any(StaffAccountId.class), any(StaffAccountRequestType.class)))
                    .thenReturn(Optional.empty());

            // When
            boolean hasPendingRequest = sut.hasPendingRequest(StaffAccountId.generate(), StaffAccountRequestType.CENSORED_ORDER_CONTENT);

            // Then
            assertThat(hasPendingRequest).isFalse();
        }

        @Test
        void shouldCatchInfraException_andTranslateToAppException() {
            // Given
            doThrow(InfraException.class)
                    .when(staffAccountRequestReadRepoMock)
                    .getPendingRequestBySubmitterAndType(any(StaffAccountId.class), any(StaffAccountRequestType.class));

            when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                    .thenReturn(mock(AppException.class));

            // When & Then
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> sut.hasPendingRequest(StaffAccountId.generate(), StaffAccountRequestType.CENSORED_ORDER_CONTENT));
        }
    }
}
