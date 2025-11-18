package com.paragon.helpers.fixtures;

import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;

import java.time.Instant;
import java.util.UUID;

public class StaffAccountSummaryModelFixture {
    private UUID id = UUID.randomUUID();
    private String username = "testuser";
    private String status = "PENDING_PASSWORD_CHANGE";
    private int orderAccessDuration = 7;
    private int modmailTranscriptAccessDuration = 14;
    private Instant createdAtUtc = Instant.now();

    public StaffAccountSummaryModelFixture withId(UUID value) {
        this.id = value;
        return this;
    }

    public StaffAccountSummaryModelFixture withUsername(String value) {
        this.username = value;
        return this;
    }

    public StaffAccountSummaryModelFixture withStatus(String value) {
        this.status = value;
        return this;
    }

    public StaffAccountSummaryModelFixture withOrderAccessDuration(int days) {
        this.orderAccessDuration = days;
        return this;
    }

    public StaffAccountSummaryModelFixture withModmailTranscriptAccessDuration(int days) {
        this.modmailTranscriptAccessDuration = days;
        return this;
    }

    public StaffAccountSummaryModelFixture withCreatedAtUtc(Instant value) {
        this.createdAtUtc = value;
        return this;
    }

    public StaffAccountSummaryReadModel build() {
        return new StaffAccountSummaryReadModel(
                id,
                username,
                status,
                orderAccessDuration,
                modmailTranscriptAccessDuration,
                createdAtUtc
        );
    }

    public static StaffAccountSummaryReadModel validStaffAccountSummary() {
        return new StaffAccountSummaryModelFixture().build();
    }
}