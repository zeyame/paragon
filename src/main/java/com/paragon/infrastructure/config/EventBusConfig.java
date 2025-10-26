package com.paragon.infrastructure.config;

import com.paragon.application.events.EventBus;
import com.paragon.application.events.audittrail.StaffAccountLockedEventAuditHandler;
import com.paragon.application.events.audittrail.StaffAccountLoggedInEventAuditHandler;
import com.paragon.application.events.audittrail.StaffAccountRegisteredEventAuditHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {
    private final EventBus eventBus;
    private final StaffAccountRegisteredEventAuditHandler staffAccountRegisteredEventAuditHandler;
    private final StaffAccountLoggedInEventAuditHandler staffAccountLoggedInEventAuditHandler;
    private final StaffAccountLockedEventAuditHandler staffAccountLockedEventAuditHandler;

    public EventBusConfig(EventBus eventBus, StaffAccountRegisteredEventAuditHandler staffAccountRegisteredEventAuditHandler,
                          StaffAccountLoggedInEventAuditHandler staffAccountLoggedInEventAuditHandler,
                          StaffAccountLockedEventAuditHandler staffAccountLockedEventAuditHandler) {
        this.eventBus = eventBus;
        this.staffAccountRegisteredEventAuditHandler = staffAccountRegisteredEventAuditHandler;
        this.staffAccountLoggedInEventAuditHandler = staffAccountLoggedInEventAuditHandler;
        this.staffAccountLockedEventAuditHandler = staffAccountLockedEventAuditHandler;
    }

    @PostConstruct
    public void registerHandlers() {
        eventBus.registerHandler(staffAccountRegisteredEventAuditHandler);
        eventBus.registerHandler(staffAccountLoggedInEventAuditHandler);
        eventBus.registerHandler(staffAccountLockedEventAuditHandler);
    }
}
