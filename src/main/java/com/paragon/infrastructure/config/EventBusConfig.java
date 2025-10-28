package com.paragon.infrastructure.config;

import com.paragon.application.events.EventBus;
import com.paragon.application.events.audittrail.StaffAccountEventAuditTrailHandler;
import com.paragon.application.events.refreshtokens.StaffAccountRefreshTokenRevocationHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {
    private final EventBus eventBus;
    private final StaffAccountEventAuditTrailHandler staffAccountEventAuditTrailHandler;
    private final StaffAccountRefreshTokenRevocationHandler staffAccountRefreshTokenRevocationHandler;

    public EventBusConfig(EventBus eventBus,
                          StaffAccountEventAuditTrailHandler staffAccountEventAuditTrailHandler,
                          StaffAccountRefreshTokenRevocationHandler staffAccountRefreshTokenRevocationHandler) {
        this.eventBus = eventBus;
        this.staffAccountEventAuditTrailHandler = staffAccountEventAuditTrailHandler;
        this.staffAccountRefreshTokenRevocationHandler = staffAccountRefreshTokenRevocationHandler;
    }

    @PostConstruct
    public void registerHandlers() {
        eventBus.registerHandler(staffAccountEventAuditTrailHandler);
        eventBus.registerHandler(staffAccountRefreshTokenRevocationHandler);
    }
}
