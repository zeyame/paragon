package com.paragon.application.events.audittrail;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.common.interfaces.RequestMetadataProvider;
import com.paragon.application.events.EventHandler;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffAccountLockedEventAuditHandler implements EventHandler<StaffAccountLockedEvent> {
    private final AuditTrailWriteRepo auditTrailWriteRepo;
    private final RequestMetadataProvider requestMetadataProvider;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountLockedEventAuditHandler.class);

    public StaffAccountLockedEventAuditHandler(AuditTrailWriteRepo auditTrailWriteRepo, RequestMetadataProvider requestMetadataProvider) {
        this.auditTrailWriteRepo = auditTrailWriteRepo;
        this.requestMetadataProvider = requestMetadataProvider;
    }

    @Override
    public void handle(StaffAccountLockedEvent event) {
        String ipAddress = requestMetadataProvider.getIpAddress();
        String correlationId = requestMetadataProvider.getCorrelationId();

        log.info("Handling StaffAccountLockedEvent for staffAccountId={} (correlationId={}, ip={})",
                event.getStaffAccountId().getValue(), correlationId, ipAddress);

        try {
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    event.getStaffAccountId(),
                    AuditEntryActionType.ACCOUNT_LOCKED,
                    AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                    AuditEntryTargetType.ACCOUNT,
                    Outcome.SUCCESS,
                    ipAddress,
                    correlationId
            );
            auditTrailWriteRepo.create(auditTrailEntry);

            log.info("AuditTrailEntry persisted for StaffAccountLockedEvent: staffAccountId={}, correlationId={}",
                    event.getStaffAccountId().getValue(), correlationId);
        }
        catch (DomainException ex) {
            log.error("Domain rule violation while handling StaffAccountLockedInEvent (staffAccountId={}): {}",
                    event.getStaffAccountId().getValue(), ex.getMessage(), ex);
        }
        catch (InfraException ex) {
            log.error("Infrastructure error occurred while persisting AuditTrailEntry for StaffAccountLockedInEvent (staffAccountId={})",
                    event.getStaffAccountId().getValue(), ex);
        }
    }

    @Override
    public List<String> subscribedToEvents() {
        return List.of(EventNames.STAFF_ACCOUNT_LOCKED);
    }
}
