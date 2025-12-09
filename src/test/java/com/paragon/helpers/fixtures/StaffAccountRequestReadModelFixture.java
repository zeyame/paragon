package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;

import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public class StaffAccountRequestReadModelFixture {
    private UUID id = UUID.randomUUID();
    private UUID submittedBy = UUID.randomUUID();
    private String submittedByUsername = "testuser";
    private StaffAccountRequestType requestType = StaffAccountRequestType.PASSWORD_CHANGE;
    private String targetId = null;
    private TargetType targetType = null;
    private StaffAccountRequestStatus status = StaffAccountRequestStatus.PENDING;
    private Instant submittedAtUtc = Instant.now();
    private Instant expiresAtUtc = Instant.now().plus(Period.ofDays(7));
    private UUID approvedBy = null;
    private String approvedByUsername = null;
    private Instant approvedAtUtc = null;
    private UUID rejectedBy = null;
    private String rejectedByUsername = null;
    private Instant rejectedAtUtc = null;

    public StaffAccountRequestReadModelFixture withId(UUID value) {
        this.id = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withSubmittedBy(UUID value) {
        this.submittedBy = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withSubmittedByUsername(String value) {
        this.submittedByUsername = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withRequestType(StaffAccountRequestType value) {
        this.requestType = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withTargetId(String value) {
        this.targetId = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withTargetType(TargetType value) {
        this.targetType = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withStatus(StaffAccountRequestStatus value) {
        this.status = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withSubmittedAtUtc(Instant value) {
        this.submittedAtUtc = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withExpiresAtUtc(Instant value) {
        this.expiresAtUtc = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withApprovedBy(UUID value) {
        this.approvedBy = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withApprovedByUsername(String value) {
        this.approvedByUsername = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withApprovedAtUtc(Instant value) {
        this.approvedAtUtc = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withRejectedBy(UUID value) {
        this.rejectedBy = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withRejectedByUsername(String value) {
        this.rejectedByUsername = value;
        return this;
    }

    public StaffAccountRequestReadModelFixture withRejectedAtUtc(Instant value) {
        this.rejectedAtUtc = value;
        return this;
    }

    public StaffAccountRequestReadModel build() {
        return new StaffAccountRequestReadModel(
                id,
                submittedBy,
                submittedByUsername,
                requestType,
                targetId,
                targetType,
                status,
                submittedAtUtc,
                expiresAtUtc,
                approvedBy,
                approvedByUsername,
                approvedAtUtc,
                rejectedBy,
                rejectedByUsername,
                rejectedAtUtc
        );
    }

    public static StaffAccountRequestReadModel validPendingRequest() {
        return new StaffAccountRequestReadModelFixture().build();
    }

    public static StaffAccountRequestReadModel approvedRequest() {
        return new StaffAccountRequestReadModelFixture()
                .withStatus(StaffAccountRequestStatus.APPROVED)
                .withApprovedBy(UUID.randomUUID())
                .withApprovedByUsername("admin")
                .withApprovedAtUtc(Instant.now())
                .build();
    }

    public static StaffAccountRequestReadModel rejectedRequest() {
        return new StaffAccountRequestReadModelFixture()
                .withStatus(StaffAccountRequestStatus.REJECTED)
                .withRejectedBy(UUID.randomUUID())
                .withRejectedByUsername("admin")
                .withRejectedAtUtc(Instant.now())
                .build();
    }

    public static StaffAccountRequestReadModel requestWithTarget() {
        String targetId = UUID.randomUUID().toString();
        return new StaffAccountRequestReadModelFixture()
                .withRequestType(StaffAccountRequestType.CENSORED_ORDER_CONTENT)
                .withTargetId(targetId)
                .withTargetType(TargetType.ORDER)
                .build();
    }
}