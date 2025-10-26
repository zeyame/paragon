package com.paragon.application.events.audittrail;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.common.interfaces.RequestMetadataProvider;
import com.paragon.application.events.EventHandler;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StaffAccountLoggedInEventAuditHandler implements EventHandler<StaffAccountLoggedInEvent> {
    private final AuditTrailWriteRepo auditTrailWriteRepo;
    private final RequestMetadataProvider requestMetadataProvider;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountLoggedInEventAuditHandler.class);

    public StaffAccountLoggedInEventAuditHandler(AuditTrailWriteRepo auditTrailWriteRepo, RequestMetadataProvider requestMetadataProvider) {
        this.auditTrailWriteRepo = auditTrailWriteRepo;
        this.requestMetadataProvider = requestMetadataProvider;
    }

    @Override
    public void handle(StaffAccountLoggedInEvent event) {
        String ipAddress = requestMetadataProvider.getIpAddress();
        String correlationId = requestMetadataProvider.getCorrelationId();

        log.info("Handling StaffAccountLoggedInEvent for staffAccountId={} (correlationId={}, ip={})",
                event.getStaffAccountId().getValue(), correlationId, ipAddress);

        try {
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    event.getStaffAccountId(),
                    AuditEntryActionType.LOGIN,
                    AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                    AuditEntryTargetType.ACCOUNT,
                    Outcome.SUCCESS,
                    ipAddress,
                    correlationId
            );
            auditTrailWriteRepo.create(auditTrailEntry);

            log.info("AuditTrailEntry persisted for StaffAccountLoggedInEvent: staffAccountId={}, correlationId={}",
                    event.getStaffAccountId().getValue(), correlationId);
        }
        catch (DomainException ex) {
            log.error("Domain rule violation while handling StaffAccountLoggedInEvent (staffAccountId={}): {}",
                    event.getStaffAccountId().getValue(), ex.getMessage(), ex);
        }
        catch (InfraException ex) {
            log.error("Infrastructure error occurred while persisting AuditTrailEntry for StaffAccountLoggedInEvent (staffAccountId={})",
                    event.getStaffAccountId().getValue(), ex);
        }
    }

    @Override
    public String subscribedToEventName() {
        return EventNames.STAFF_ACCOUNT_LOGGED_IN;
    }
}
