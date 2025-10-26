package com.paragon.api.errorhandling;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GlobalExceptionFilterTests {
    @Nested
    class HandleAppException {
        private final GlobalExceptionFilter sut;

        public HandleAppException() {
            sut = new GlobalExceptionFilter();
        }

        @ParameterizedTest
        @MethodSource("appExceptions")
        void appExceptionThrown_shouldMapToCorrectResponse(AppException appException, HttpStatus expectedStatusCode) {
            // When
            ResponseEntity<ResponseDto<Void>> responseEntity = sut.handleAppException(appException);

            // Then
            assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatusCode);

            ResponseDto<Void> responseDto = responseEntity.getBody();
            assertThat(responseDto.result()).isNull();
            assertThat(responseDto.errorDto()).isNotNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto.message()).isEqualTo(appException.getMessage());
            assertThat(errorDto.code()).isEqualTo(appException.getErrorCode());
        }

        private static Stream<Arguments> appExceptions() {
            return Stream.of(
                    Arguments.of(new AppException(AppExceptionInfo.staffAccountNotFound("staff-id")), HttpStatus.NOT_FOUND),
                    Arguments.of(new AppException(AppExceptionInfo.staffAccountUsernameAlreadyExists("john_doe")), HttpStatus.CONFLICT),
                    Arguments.of(new AppException(AppExceptionInfo.invalidLoginCredentials()), HttpStatus.UNAUTHORIZED)
            );
        }
    }
}
