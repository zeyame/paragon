package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.models.valueobjects.StaffAccountRequestId;

public class StaffAccountRequest extends EventSourcedAggregate<DomainEvent, StaffAccountRequestId> {
    private StaffAccountRequest(StaffAccountRequestId id) {
        super(id);
    }
}
