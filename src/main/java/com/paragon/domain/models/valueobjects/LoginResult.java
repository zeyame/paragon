package com.paragon.domain.models.valueobjects;

public record LoginResult(boolean success, String failureReason) {

    public static LoginResult ofSuccess() {
        return new LoginResult(true, null);
    }

    public static LoginResult ofFailure() {
        return new LoginResult(false, "Invalid credentials");
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return !success;
    }
}