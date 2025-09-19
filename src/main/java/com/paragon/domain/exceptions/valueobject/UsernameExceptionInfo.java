package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class UsernameExceptionInfo extends DomainExceptionInfo {
    private UsernameExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static UsernameExceptionInfo mustNotBeBlank() {
        return new UsernameExceptionInfo("Username cannot be null or blank.", 103001);
    }

    public static UsernameExceptionInfo lengthOutOfRange() {
        return new UsernameExceptionInfo("Username must be between 3 and 20 characters long.", 103002);
    }

    public static UsernameExceptionInfo invalidCharacters() {
        return new UsernameExceptionInfo("Username can only contain letters, numbers, and underscores.", 103003);
    }

    public static UsernameExceptionInfo consecutiveUnderscores() {
        return new UsernameExceptionInfo("Username cannot contain consecutive underscores.", 103004);
    }

    public static UsernameExceptionInfo mustStartWithALetter() {
        return new UsernameExceptionInfo("Username must start with a letter.", 103005);
    }

    public static UsernameExceptionInfo mustNotEndWithUnderscore() {
        return new UsernameExceptionInfo("Username must not end with an underscore.", 103006);
    }

    public static UsernameExceptionInfo reservedWord() {
        return new UsernameExceptionInfo("This is a reserved word and cannot be assigned as a username.", 103007);
    }
}
