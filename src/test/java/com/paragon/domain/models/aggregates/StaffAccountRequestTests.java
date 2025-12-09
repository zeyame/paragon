package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountrequestevents.StaffAccountRequestEventBase;
import com.paragon.domain.events.staffaccountrequestevents.StaffAccountRequestSubmittedEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountRequestException;
import com.paragon.domain.exceptions.aggregate.StaffAccountRequestExceptionInfo;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.TargetId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class StaffAccountRequestTests {
    StaffAccountId submittedBy;
    StaffAccountRequestType requestType;
    TargetId targetId;
    TargetType targetType;
    DateTimeUtc expiresAt;

    public StaffAccountRequestTests() {
        submittedBy = StaffAccountId.generate();
        requestType = StaffAccountRequestType.PASSWORD_CHANGE;
        targetId = TargetId.of(submittedBy.getValue().toString());
        targetType = TargetType.ACCOUNT;
        expiresAt = DateTimeUtc.of(Instant.now().plus(Period.ofDays(7)));
    }

    @Nested
    class Submit {
        @Test
        void shouldSubmitRequest() {
            // When
            StaffAccountRequest request = StaffAccountRequest.submit(submittedBy, requestType, targetId, targetType);

            // Then
            assertThat(request.getId()).isNotNull();
            assertThat(request.getSubmittedBy()).isEqualTo(submittedBy);
            assertThat(request.getRequestType()).isEqualTo(requestType);
            assertThat(request.getTargetId()).isEqualTo(targetId);
            assertThat(request.getTargetType()).isEqualTo(targetType);
            assertThat(request.getStatus()).isEqualTo(StaffAccountRequestStatus.PENDING);
            assertThat(request.getSubmittedAt()).isNotNull();
            assertThat(request.getExpiresAt().getValue()).isAfterOrEqualTo(expiresAt.getValue());
            assertThat(request.getApprovedBy()).isNull();
            assertThat(request.getApprovedAt()).isNull();
            assertThat(request.getRejectedBy()).isNull();
            assertThat(request.getRejectedAt()).isNull();
            assertThat(request.getVersion().getValue()).isEqualTo(1);
        }

        @Test
        void shouldSubmitRequestWithoutTargetIdAndType() {
            // When
            StaffAccountRequest request = StaffAccountRequest.submit(submittedBy, requestType, null, null);

            // Then
            assertThat(request.getId()).isNotNull();
            assertThat(request.getSubmittedBy()).isEqualTo(submittedBy);
            assertThat(request.getRequestType()).isEqualTo(requestType);
            assertThat(request.getTargetId()).isNull();
            assertThat(request.getTargetType()).isNull();
            assertThat(request.getStatus()).isEqualTo(StaffAccountRequestStatus.PENDING);
            assertThat(request.getSubmittedAt()).isNotNull();
            assertThat(request.getExpiresAt().getValue()).isAfterOrEqualTo(expiresAt.getValue());
            assertThat(request.getApprovedBy()).isNull();
            assertThat(request.getApprovedAt()).isNull();
            assertThat(request.getRejectedBy()).isNull();
            assertThat(request.getRejectedAt()).isNull();
            assertThat(request.getVersion().getValue()).isEqualTo(1);
        }

        @Test
        void shouldEnqueueStaffAccountRequestSubmittedEvent() {
            // When
            StaffAccountRequest request = StaffAccountRequest.submit(submittedBy, requestType, targetId, targetType);

            // Then
            List<DomainEvent> events = request.dequeueUncommittedEvents();
            assertThat(events).isNotEmpty();
            StaffAccountRequestSubmittedEvent submittedEvent = (StaffAccountRequestSubmittedEvent) events.getFirst();
            assertThatEventDataIsCorrect(submittedEvent, request);
        }

        @Test
        void givenMissingSubmittedBy_submissionShouldFail() {
            // Given
            StaffAccountRequestException expectedException = new StaffAccountRequestException(StaffAccountRequestExceptionInfo.submittedByRequired());

            // When & Then
            assertThatExceptionOfType(StaffAccountRequestException.class)
                    .isThrownBy(() -> StaffAccountRequest.submit(null, requestType, targetId, targetType))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void givenMissingRequestType_submissionShouldFai() {
            // Given
            StaffAccountRequestException expectedException = new StaffAccountRequestException(
                    StaffAccountRequestExceptionInfo.requestTypeRequired()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountRequestException.class)
                    .isThrownBy(() -> StaffAccountRequest.submit(submittedBy, null, targetId, targetType))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void giveOnlyTargetIdProvidedWithoutTargetType_submissionShouldFail() {
            // Given
            StaffAccountRequestException expectedException = new StaffAccountRequestException(
                    StaffAccountRequestExceptionInfo.targetIdAndTypeMustBeBothProvidedOrBothNull()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountRequestException.class)
                    .isThrownBy(() -> StaffAccountRequest.submit(submittedBy, requestType, targetId, null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void giveOnlyTargetTypeProvidedWithoutTargetId_submissionShouldFail() {
            // Given
            StaffAccountRequestException expectedException = new StaffAccountRequestException(
                    StaffAccountRequestExceptionInfo.targetIdAndTypeMustBeBothProvidedOrBothNull()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountRequestException.class)
                    .isThrownBy(() -> StaffAccountRequest.submit(submittedBy, requestType, null, targetType))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }
    }

    private static void assertThatEventDataIsCorrect(StaffAccountRequestEventBase event, StaffAccountRequest request) {
        assertThat(event.getStaffAccountRequestId()).isEqualTo(request.getId());
        assertThat(event.getSubmittedBy()).isEqualTo(request.getSubmittedBy());
        assertThat(event.getRequestType()).isEqualTo(request.getRequestType());
        assertThat(event.getTargetId()).isEqualTo(request.getTargetId());
        assertThat(event.getTargetType()).isEqualTo(request.getTargetType());
        assertThat(event.getStatus()).isEqualTo(request.getStatus());
        assertThat(event.getSubmittedAt()).isEqualTo(request.getSubmittedAt());
        assertThat(event.getExpiresAt()).isEqualTo(request.getExpiresAt());
        assertThat(event.getApprovedBy()).isEqualTo(request.getApprovedBy());
        assertThat(event.getApprovedAt()).isEqualTo(request.getApprovedAt());
        assertThat(event.getRejectedBy()).isEqualTo(request.getRejectedBy());
        assertThat(event.getRejectedAt()).isEqualTo(request.getRejectedAt());
        assertThat(event.getVersion()).isEqualTo(request.getVersion());
    }
}
