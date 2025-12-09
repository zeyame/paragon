package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public class StaffAccountRequestFixture {
    private String id = UUID.randomUUID().toString();
    private String submittedBy = UUID.randomUUID().toString();
    private StaffAccountRequestType requestType = StaffAccountRequestType.PASSWORD_CHANGE;
    private String targetId = null;
    private TargetType targetType = null;
    private StaffAccountRequestStatus status = StaffAccountRequestStatus.PENDING;
    private Instant submittedAt = Instant.now();
    private Instant expiresAt = Instant.now().plus(Period.ofDays(7));
    private String approvedBy = null;
    private Instant approvedAt = null;
    private String rejectedBy = null;
    private Instant rejectedAt = null;
    private int version = 1;

    public StaffAccountRequestFixture withId(String value) {
        this.id = value;
        return this;
    }

    public StaffAccountRequestFixture withSubmittedBy(String value) {
        this.submittedBy = value;
        return this;
    }

    public StaffAccountRequestFixture withRequestType(StaffAccountRequestType value) {
        this.requestType = value;
        return this;
    }

    public StaffAccountRequestFixture withTargetId(String value) {
        this.targetId = value;
        return this;
    }

    public StaffAccountRequestFixture withTargetType(TargetType value) {
        this.targetType = value;
        return this;
    }

    public StaffAccountRequestFixture withStatus(StaffAccountRequestStatus value) {
        this.status = value;
        return this;
    }

    public StaffAccountRequestFixture withSubmittedAt(Instant value) {
        this.submittedAt = value;
        return this;
    }

    public StaffAccountRequestFixture withExpiresAt(Instant value) {
        this.expiresAt = value;
        return this;
    }

    public StaffAccountRequestFixture withApprovedBy(String value) {
        this.approvedBy = value;
        return this;
    }

    public StaffAccountRequestFixture withApprovedAt(Instant value) {
        this.approvedAt = value;
        return this;
    }

    public StaffAccountRequestFixture withRejectedBy(String value) {
        this.rejectedBy = value;
        return this;
    }

    public StaffAccountRequestFixture withRejectedAt(Instant value) {
        this.rejectedAt = value;
        return this;
    }

    public StaffAccountRequestFixture withVersion(int value) {
        this.version = value;
        return this;
    }

    public StaffAccountRequest build() {
        return StaffAccountRequest.createFrom(
                StaffAccountRequestId.from(id),
                StaffAccountId.from(submittedBy),
                requestType,
                targetId != null ? TargetId.of(targetId) : null,
                targetType,
                status,
                DateTimeUtc.of(submittedAt),
                DateTimeUtc.of(expiresAt),
                approvedBy != null ? StaffAccountId.from(approvedBy) : null,
                approvedAt != null ? DateTimeUtc.of(approvedAt) : null,
                rejectedBy != null ? StaffAccountId.from(rejectedBy) : null,
                rejectedAt != null ? DateTimeUtc.of(rejectedAt) : null,
                Version.of(version)
        );
    }

    public static StaffAccountRequest validStaffAccountRequest() {
        return new StaffAccountRequestFixture().build();
    }

    public static StaffAccountRequest approvedStaffAccountRequest() {
        return new StaffAccountRequestFixture()
                .withStatus(StaffAccountRequestStatus.APPROVED)
                .withApprovedBy(UUID.randomUUID().toString())
                .withApprovedAt(Instant.now())
                .build();
    }

    public static StaffAccountRequest rejectedStaffAccountRequest() {
        return new StaffAccountRequestFixture()
                .withStatus(StaffAccountRequestStatus.REJECTED)
                .withRejectedBy(UUID.randomUUID().toString())
                .withRejectedAt(Instant.now())
                .build();
    }

    public static StaffAccountRequest requestWithTarget() {
        String targetId = UUID.randomUUID().toString();
        return new StaffAccountRequestFixture()
                .withRequestType(StaffAccountRequestType.CENSORED_ORDER_CONTENT)
                .withTargetId(targetId)
                .withTargetType(TargetType.ORDER)
                .build();
    }
}