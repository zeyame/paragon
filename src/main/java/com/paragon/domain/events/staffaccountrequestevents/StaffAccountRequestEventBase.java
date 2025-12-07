package com.paragon.domain.events.staffaccountrequestevents;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

@Getter
public abstract class StaffAccountRequestEventBase extends DomainEvent {

    private final StaffAccountRequestId staffAccountRequestId;
    private final StaffAccountId submittedBy;
    private final StaffAccountRequestType requestType;
    private final TargetId targetId;
    private final TargetType targetType;
    private final StaffAccountRequestStatus status;
    private final DateTimeUtc submittedAt;
    private final DateTimeUtc expiresAt;
    private final StaffAccountId approvedBy;
    private final DateTimeUtc approvedAt;
    private final StaffAccountId rejectedBy;
    private final DateTimeUtc rejectedAt;
    private final Version version;

    protected StaffAccountRequestEventBase(StaffAccountRequest staffAccountRequest, String eventName) {
        super(EventId.generate(), eventName);

        this.staffAccountRequestId = staffAccountRequest.getId();
        this.submittedBy = staffAccountRequest.getSubmittedBy();
        this.requestType = staffAccountRequest.getRequestType();
        this.targetId = staffAccountRequest.getTargetId();
        this.targetType = staffAccountRequest.getTargetType();
        this.status = staffAccountRequest.getStatus();
        this.submittedAt = staffAccountRequest.getSubmittedAt();
        this.expiresAt = staffAccountRequest.getExpiresAt();
        this.approvedBy = staffAccountRequest.getApprovedBy();
        this.approvedAt = staffAccountRequest.getApprovedAt();
        this.rejectedBy = staffAccountRequest.getRejectedBy();
        this.rejectedAt = staffAccountRequest.getRejectedAt();
        this.version = staffAccountRequest.getVersion();
    }
}
