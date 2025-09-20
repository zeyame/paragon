package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class StaffAccountIdTests {

    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidStaffAccountId() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();

            // When & Then
            assertThat(staffAccountId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }

    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidStaffAccountId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(StaffAccountId.from(validUuidString).getValue())
                    .isEqualTo(expectedUuid);
        }

        @Test
        void givenEmptyStringId_whenFromIsCalled_thenThrowsStaffAccountIdException() {
            // Given
            String id = "";
            String expectedErrorMessage = StaffAccountIdExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = StaffAccountIdExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountIdException.class)
                    .isThrownBy(() -> StaffAccountId.from(id))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenInvalidStringIdFormat_whenFromIsCalled_thenThrowsStaffAccountIdException() {
            // Given
            String id = "invalid-id";
            String expectedErrorMessage = StaffAccountIdExceptionInfo.invalidFormat().getMessage();
            int expectedErrorCode = StaffAccountIdExceptionInfo.invalidFormat().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountIdException.class)
                    .isThrownBy(() -> StaffAccountId.from(id))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
