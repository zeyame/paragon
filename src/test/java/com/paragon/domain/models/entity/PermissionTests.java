package com.paragon.domain.models.entity;

import com.paragon.domain.enums.PermissionCategory;
import com.paragon.domain.exceptions.entity.PermissionException;
import com.paragon.domain.exceptions.entity.PermissionExceptionInfo;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

public class PermissionTests {
    @Nested
    class Create {
        private final PermissionCode code;
        private final PermissionCategory category;

        Create() {
            code = SystemPermissions.MANAGE_ACCOUNTS;
            category = PermissionCategory.ACCOUNTS;
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "Some description of the permission"})
        void givenValidInputWithOrWithoutDescription_shouldCreatePermission(String description) {
            // Given
            Permission permission = Permission.create(code, category, description);

            // Then
            assertThat(permission).isNotNull();
            assertThat(permission.getCode()).isEqualTo(code);
            assertThat(permission.getCategory()).isEqualTo(category);
            assertThat(permission.getDescription()).isEqualTo(description);
        }

        @Test
        void givenMissingCode_creationShouldFail() {
            // Given
            String expectedErrorMessage = PermissionExceptionInfo.codeRequired().getMessage();
            int expectedErrorCode = PermissionExceptionInfo.codeRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(PermissionException.class)
                    .isThrownBy(() -> Permission.create(null, category, ""))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingCategory_creationShouldFail() {
            // Given
            String expectedErrorMessage = PermissionExceptionInfo.categoryRequired().getMessage();
            int expectedErrorCode = PermissionExceptionInfo.categoryRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(PermissionException.class)
                    .isThrownBy(() -> Permission.create(code, null, ""))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
