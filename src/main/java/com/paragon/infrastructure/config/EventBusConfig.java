package com.paragon.infrastructure.config;

import com.paragon.application.events.EventBus;
import com.paragon.application.events.audittrail.StaffAccountRegisteredEventAuditHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {
    private final EventBus eventBus;
    private final StaffAccountRegisteredEventAuditHandler staffAccountRegisteredEventAuditHandler;

    public EventBusConfig(EventBus eventBus, StaffAccountRegisteredEventAuditHandler staffAccountRegisteredEventAuditHandler) {
        this.eventBus = eventBus;
        this.staffAccountRegisteredEventAuditHandler = staffAccountRegisteredEventAuditHandler;
    }

    @PostConstruct
    public void registerHandlers() {
        eventBus.registerHandler(staffAccountRegisteredEventAuditHandler);
    }
}
