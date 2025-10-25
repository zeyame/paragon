package com.paragon.application.events.audittrail;

import com.paragon.application.common.interfaces.RequestMetadataProvider;
import com.paragon.application.events.EventHandler;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StaffAccountRegisteredEventAuditHandler implements EventHandler<StaffAccountRegisteredEvent> {
    private final AuditTrailWriteRepo auditTrailWriteRepo;
    private final RequestMetadataProvider requestMetadataProvider;

    public StaffAccountRegisteredEventAuditHandler(AuditTrailWriteRepo auditTrailWriteRepo, RequestMetadataProvider requestMetadataProvider) {
        this.auditTrailWriteRepo = auditTrailWriteRepo;
        this.requestMetadataProvider = requestMetadataProvider;
    }

    @Override
    public void handle(StaffAccountRegisteredEvent event) {
        String ipAddress = requestMetadataProvider.getIpAddress();
        String correlationId = requestMetadataProvider.getCorrelationId();

        log.info("Handling StaffAccountRegisteredEvent for staffAccountId={} (correlationId={}, ip={})",
                event.getStaffAccountId().getValue(), correlationId, ipAddress);

        try {
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    event.getStaffAccountCreatedBy(),
                    AuditEntryActionType.REGISTER_ACCOUNT,
                    AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                    AuditEntryTargetType.ACCOUNT,
                    Outcome.SUCCESS,
                    ipAddress,
                    correlationId
            );
            auditTrailWriteRepo.create(auditTrailEntry);

            log.info("AuditTrailEntry persisted for StaffAccountRegisteredEvent: staffAccountId={}, actorId={}, correlationId={}",
                    event.getStaffAccountId().getValue(), event.getStaffAccountCreatedBy().getValue(), correlationId);
        }
        catch (DomainException ex) {
            log.error("Domain rule violation while handling StaffAccountRegisteredEvent (staffAccountId={}): {}",
                    event.getStaffAccountId().getValue(), ex.getMessage(), ex);
        }
        catch (InfraException ex) {
            log.error("Infrastructure error occurred while persisting AuditTrailEntry for StaffAccountRegisteredEvent (staffAccountId={})",
                    event.getStaffAccountId().getValue(), ex);
        }
    }

    @Override
    public String subscribedToEventName() {
        return EventNames.STAFF_ACCOUNT_REGISTERED;
    }
}
