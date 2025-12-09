package com.paragon.application.events.refreshtokens;

import com.paragon.application.events.EventHandler;
import com.paragon.application.services.StaffAccountRefreshTokenRevocationService;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountEventBase;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffAccountRefreshTokenRevocationHandler implements EventHandler<StaffAccountEventBase> {
    private final StaffAccountRefreshTokenRevocationService revocationService;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountRefreshTokenRevocationHandler.class);

    public StaffAccountRefreshTokenRevocationHandler(StaffAccountRefreshTokenRevocationService revocationService) {
        this.revocationService = revocationService;
    }

    @Override
    public void handle(StaffAccountEventBase event) {
        try {
            log.info("Attempting to revoke all active refresh tokens for staff account with ID: {}, in event: {}",
                    event.getStaffAccountId().getValue(), event.getEventName());
            revocationService.revokeAllTokensForStaffAccount(event.getStaffAccountId());
            log.info("Successfully revoked all active refresh tokens for staff account with ID: {}, in event: {}",
                    event.getStaffAccountId().getValue(), event.getEventName());
        } catch (DomainException ex) {
            log.error("Domain rule violation while revoking refresh tokens for staff account with ID={}, in event: {}. Error reason: {}",
                    event.getStaffAccountId().getValue(), event.getEventName(), ex.getMessage(), ex);
        } catch (InfraException ex) {
            log.error("Infrastructure related error occurred while revoking refresh tokens for staff account with ID={}, in event: {}. Error reason: {}",
                    event.getStaffAccountId().getValue(), event.getEventName(), ex.getMessage(), ex);
        }
    }

    @Override
    public List<String> subscribedToEvents() {
        return List.of(
                EventNames.STAFF_ACCOUNT_LOCKED,
                EventNames.STAFF_ACCOUNT_DISABLED,
                EventNames.STAFF_ACCOUNT_PASSWORD_RESET
        );
    }
}
