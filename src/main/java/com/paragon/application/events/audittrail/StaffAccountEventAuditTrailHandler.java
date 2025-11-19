package com.paragon.application.events.audittrail;

import com.paragon.application.events.EventHandler;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountEventBase;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffAccountEventAuditTrailHandler implements EventHandler<StaffAccountEventBase> {
    private final AuditTrailWriteRepo auditTrailWriteRepo;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountEventAuditTrailHandler.class);

    public StaffAccountEventAuditTrailHandler(AuditTrailWriteRepo auditTrailWriteRepo) {
        this.auditTrailWriteRepo = auditTrailWriteRepo;
    }

    @Override
    public void handle(StaffAccountEventBase event) {
        log.info("Handling {} for staffAccountId={} - creating audit trail entry",
                event.getEventName(), event.getStaffAccountId().getValue());

        try {
            AuditTrailEntry auditTrailEntry = AuditTrailEntryFactory.fromStaffAccountEvent(event);
            auditTrailWriteRepo.create(auditTrailEntry);

            log.info("AuditTrailEntry persisted for {}: staffAccountId={}, actionType={}",
                    event.getEventName(), event.getStaffAccountId().getValue(), auditTrailEntry.getActionType());
        } catch (DomainException ex) {
            log.error("Domain rule violation while handling {} (staffAccountId={}): {}",
                    event.getEventName(), event.getStaffAccountId().getValue(), ex.getMessage(), ex);
        } catch (InfraException ex) {
            log.error("Infrastructure error occurred while persisting AuditTrailEntry for {} (staffAccountId={})",
                    event.getEventName(), event.getStaffAccountId().getValue(), ex);
        }
    }

    @Override
    public List<String> subscribedToEvents() {
        return List.of(
                EventNames.STAFF_ACCOUNT_REGISTERED,
                EventNames.STAFF_ACCOUNT_LOGGED_IN,
                EventNames.STAFF_ACCOUNT_LOCKED,
                EventNames.STAFF_ACCOUNT_DISABLED,
                EventNames.STAFF_ACCOUNT_ENABLED,
                EventNames.STAFF_ACCOUNT_PASSWORD_RESET
        );
    }
}