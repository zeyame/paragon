package com.paragon.application.events.refreshtokens;

import com.paragon.application.events.EventHandler;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceException;
import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceExceptionInfo;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.services.RefreshTokenRevocationService;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffAccountRefreshTokenRevocationHandler implements EventHandler<StaffAccountLockedEvent> {
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountRefreshTokenRevocationHandler.class);

    public StaffAccountRefreshTokenRevocationHandler(RefreshTokenWriteRepo refreshTokenWriteRepo) {
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
    }

    @Override
    public void handle(StaffAccountLockedEvent event) {
        try {
            log.info("Attempting to revoke all active refresh tokens for staff account with ID: {}, in event: {}",
                    event.getStaffAccountId().getValue(), event.getEventName());

            List<RefreshToken> activeTokens = refreshTokenWriteRepo.getActiveTokensByStaffAccountId(event.getStaffAccountId());
            activeTokens.forEach(RefreshToken::revoke);
            refreshTokenWriteRepo.updateAll(activeTokens);

            log.info("Successfully revoked {} active refresh token(s) for staff account with ID: {}, in event: {}",
                    activeTokens.size(), event.getStaffAccountId().getValue(), event.getEventName());
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
        return List.of(EventNames.STAFF_ACCOUNT_LOCKED);
    }
}
