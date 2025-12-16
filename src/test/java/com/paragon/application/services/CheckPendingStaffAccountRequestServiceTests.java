package com.paragon.application.services;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
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

import static org.assertj.core.api.Assertions.*;
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
            sut.ensureNoPendingRequest(staffAccountId, requestType);

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
        void shouldNotThrow_whenPendingRequestDoesNotExist() {
            // Given
            when(staffAccountRequestReadRepoMock.getPendingRequestBySubmitterAndType(any(StaffAccountId.class), any(StaffAccountRequestType.class)))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatNoException().isThrownBy(() ->
                    sut.ensureNoPendingRequest(StaffAccountId.generate(), StaffAccountRequestType.CENSORED_ORDER_CONTENT)
            );
        }

        @Test
        void shouldThrow_whenPendingRequestExists() {
            // Given
            StaffAccountRequestReadModel existingRequest = StaffAccountRequestReadModelFixture.validPendingRequest();
            when(staffAccountRequestReadRepoMock.getPendingRequestBySubmitterAndType(any(StaffAccountId.class), any(StaffAccountRequestType.class)))
                    .thenReturn(Optional.of(existingRequest));
            AppException expectedException = new AppException(AppExceptionInfo.pendingStaffAccountRequestAlreadyExists(
                    existingRequest.submittedByUsername(),
                    existingRequest.requestType().toString()
            ));

            // When & Then
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> sut.ensureNoPendingRequest(StaffAccountId.of(existingRequest.submittedBy()), existingRequest.requestType()))
                    .extracting("message", "errorCode", "statusCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getErrorCode(), expectedException.getStatusCode());
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
                    .isThrownBy(() -> sut.ensureNoPendingRequest(StaffAccountId.generate(), StaffAccountRequestType.CENSORED_ORDER_CONTENT));
        }
    }
}
