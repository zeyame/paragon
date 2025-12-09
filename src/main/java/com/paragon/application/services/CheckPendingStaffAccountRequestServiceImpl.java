package com.paragon.application.services;

import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.CheckPendingStaffAccountRequestService;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountRequestReadRepo;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckPendingStaffAccountRequestServiceImpl implements CheckPendingStaffAccountRequestService {
    private final StaffAccountRequestReadRepo staffAccountRequestReadRepo;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(CheckPendingStaffAccountRequestServiceImpl.class);

    public CheckPendingStaffAccountRequestServiceImpl(StaffAccountRequestReadRepo staffAccountRequestReadRepo, AppExceptionHandler appExceptionHandler) {
        this.staffAccountRequestReadRepo = staffAccountRequestReadRepo;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public boolean hasPendingRequest(StaffAccountId staffAccountId, StaffAccountRequestType requestType) {
        try {
            return staffAccountRequestReadRepo
                    .getPendingRequestBySubmitterAndType(staffAccountId, requestType)
                    .isPresent();
        } catch (InfraException ex) {
            log.error("Failed to check for pending request for staffAccountId: {} and requestType: {}. Error: {}",
                    staffAccountId.getValue(), requestType, ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
