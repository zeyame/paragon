package com.paragon.application.events.refreshtokens;

import com.paragon.application.events.EventHandler;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.services.RefreshTokenRevocationService;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffAccountRefreshTokenRevocationHandler implements EventHandler<StaffAccountLockedEvent> {
    private final RefreshTokenRevocationService refreshTokenRevocationService;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountRefreshTokenRevocationHandler.class);

    public StaffAccountRefreshTokenRevocationHandler(RefreshTokenRevocationService refreshTokenRevocationService) {
        this.refreshTokenRevocationService = refreshTokenRevocationService;
    }

    @Override
    public void handle(StaffAccountLockedEvent event) {
        log.info("Handling StaffAccountLockedEvent for staffAccountId={} - revoking all refresh tokens",
                event.getStaffAccountId().getValue());

        try {
            refreshTokenRevocationService.revokeAllTokensForStaffAccount(event.getStaffAccountId());

            log.info("Successfully revoked all refresh tokens for staffAccountId={}",
                    event.getStaffAccountId().getValue());
        } catch (DomainException ex) {
            log.error("Domain rule violation while revoking refresh tokens for staffAccountId={}: {}",
                    event.getStaffAccountId().getValue(), ex.getMessage(), ex);
        } catch (InfraException ex) {
            log.error("Infrastructure error occurred while revoking refresh tokens for staffAccountId={}",
                    event.getStaffAccountId().getValue(), ex);
        }
    }

    @Override
    public List<String> subscribedToEvents() {
        return List.of(EventNames.STAFF_ACCOUNT_LOCKED);
    }
}
