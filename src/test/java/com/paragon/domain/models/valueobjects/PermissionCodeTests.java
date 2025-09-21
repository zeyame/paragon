package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PermissionCodeException;
import com.paragon.domain.exceptions.valueobject.PermissionCodeExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class PermissionCodeTests {
    @Nested
    class Of {
        @ParameterizedTest
        @MethodSource("validInputs")
        void givenValidStringCodes_whenOfIsCalled_thenCreatesPermissionCodes(String inputCode, String expectedCode) {
            // When
            PermissionCode permissionCode = PermissionCode.of(inputCode);

            // Then
            assertThat(permissionCode.getValue()).isEqualTo(expectedCode);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringCodes_whenOfIsCalled_thenThrowsPermissionCodeException(String invalidCode, PermissionCodeExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(PermissionCodeException.class)
                    .isThrownBy(() -> PermissionCode.of(invalidCode))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> validInputs() {
            return Stream.of(
                    Arguments.of("MANAGE_ACCOUNTS" , "MANAGE_ACCOUNTS"),
                    Arguments.of("view_order_list", "VIEW_ORDER_LIST"),
                    Arguments.of("RESET_account_PASSWORD", "RESET_ACCOUNT_PASSWORD")
            );
        }
        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    // missing values
                    Arguments.of(null, PermissionCodeExceptionInfo.missingValue()),
                    Arguments.of("", PermissionCodeExceptionInfo.missingValue()),
                    Arguments.of("   ", PermissionCodeExceptionInfo.missingValue()),

                    // length out of range
                    Arguments.of("AB", PermissionCodeExceptionInfo.lengthOutOfRange()),
                    Arguments.of("A".repeat(51), PermissionCodeExceptionInfo.lengthOutOfRange()),

                    // invalid characters
                    Arguments.of("MANAGE-ACCOUNTS", PermissionCodeExceptionInfo.invalidCharacters()),
                    Arguments.of("VIEW.ACCOUNTS", PermissionCodeExceptionInfo.invalidCharacters()),
                    Arguments.of("RESET PASSWORD", PermissionCodeExceptionInfo.invalidCharacters()),

                    // must not start or end with underscore
                    Arguments.of("_STARTWITH", PermissionCodeExceptionInfo.mustNotStartOrEndWithUnderscore()),
                    Arguments.of("ENDWITH_", PermissionCodeExceptionInfo.mustNotStartOrEndWithUnderscore()),

                    // consecutive underscores
                    Arguments.of("VIEW__ORDERS", PermissionCodeExceptionInfo.consecutiveUnderscores()),
                    Arguments.of("RESET__PASSWORD", PermissionCodeExceptionInfo.consecutiveUnderscores())
            );
        }
    }
}
