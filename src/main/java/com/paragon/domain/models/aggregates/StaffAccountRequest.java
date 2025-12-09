package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountrequestevents.StaffAccountRequestSubmittedEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountRequestException;
import com.paragon.domain.exceptions.aggregate.StaffAccountRequestExceptionInfo;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

import java.time.Instant;
import java.time.Period;

@Getter
public class StaffAccountRequest extends EventSourcedAggregate<DomainEvent, StaffAccountRequestId> {
    private final StaffAccountId submittedBy;
    private final StaffAccountRequestType requestType;
    private final TargetId targetId;
    private final TargetType targetType;
    private StaffAccountRequestStatus status;
    private final DateTimeUtc submittedAt;
    private final DateTimeUtc expiresAt;
    private StaffAccountId approvedBy;
    private DateTimeUtc approvedAt;
    private StaffAccountId rejectedBy;
    private DateTimeUtc rejectedAt;
    private static final Period EXPIRY_DURATION = Period.ofDays(7);

    private StaffAccountRequest(StaffAccountRequestId id, StaffAccountId submittedBy, StaffAccountRequestType requestType,
                                TargetId targetId, TargetType targetType, StaffAccountRequestStatus status, DateTimeUtc submittedAt,
                                DateTimeUtc expiresAt, StaffAccountId approvedBy, DateTimeUtc approvedAt, StaffAccountId rejectedBy,
                                DateTimeUtc rejectedAt, Version version) {
        super(id);
        this.submittedBy = submittedBy;
        this.requestType = requestType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.status = status;
        this.submittedAt = submittedAt;
        this.expiresAt = expiresAt;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = rejectedAt;
        this.version = version;
    }

    public static StaffAccountRequest submit(StaffAccountId submittedBy, StaffAccountRequestType requestType, TargetId targetId, TargetType targetType) {
        assertValidRequestSubmission(submittedBy, requestType, targetId, targetType);
        StaffAccountRequest request = new StaffAccountRequest(
                StaffAccountRequestId.generate(), submittedBy, requestType, targetId,
                targetType, StaffAccountRequestStatus.PENDING, DateTimeUtc.now(), DateTimeUtc.of(Instant.now().plus(EXPIRY_DURATION)),
                null, null, null, null, Version.initial()
        );
        request.enqueue(new StaffAccountRequestSubmittedEvent(request));
        return request;
    }

    private static void assertValidRequestSubmission(StaffAccountId submittedBy, StaffAccountRequestType requestType,
                                                     TargetId targetId, TargetType targetType) {
        if (submittedBy == null) {
            throw new StaffAccountRequestException(StaffAccountRequestExceptionInfo.submittedByRequired());
        }
        if (requestType == null) {
            throw new StaffAccountRequestException(StaffAccountRequestExceptionInfo.requestTypeRequired());
        }

        boolean hasTargetId = targetId != null;
        boolean hasTargetType = targetType != null;
        if (hasTargetId != hasTargetType) {
            throw new StaffAccountRequestException(
                    StaffAccountRequestExceptionInfo.targetIdAndTypeMustBeBothProvidedOrBothNull()
            );
        }
    }
}
