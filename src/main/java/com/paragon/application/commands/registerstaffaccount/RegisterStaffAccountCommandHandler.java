package com.paragon.application.commands.registerstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.queries.repositoryinterfaces.PermissionReadRepo;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RegisterStaffAccountCommandHandler implements CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final PermissionReadRepo permissionReadRepo;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(RegisterStaffAccountCommandHandler.class);

    RegisterStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, PermissionReadRepo permissionReadRepo, AppExceptionHandler appExceptionHandler) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.permissionReadRepo = permissionReadRepo;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public RegisterStaffAccountCommandResponse handle(RegisterStaffAccountCommand command) {
        try {
            Optional<StaffAccount> optional = staffAccountWriteRepo.getById(StaffAccountId.from(command.id()));
            if (optional.isEmpty()) {
                log.error("Staff account registration failed: requestingStaffId='{}' does not exist.", command.id());
                throw new AppException(AppExceptionInfo.staffAccountNotFound(command.id()));
            }
            StaffAccount requestingStaffAccount = optional.get();

            Permission permission = permissionReadRepo.getByCode(SystemPermissions.MANAGE_ACCOUNTS).get();
            if (!requestingStaffAccount.hasPermission(permission.getId())) {
                log.warn("Staff account registration request denied: requestingStaffId='{}' lacked MANAGE_ACCOUNTS permission.", command.id());
                throw new AppException(AppExceptionInfo.permissionAccessDenied("registration"));
            }


            StaffAccount staffAccount = StaffAccount.register(
                    Username.of(command.username()),
                    command.email() != null ? Email.of(command.email()) : null,
                    Password.of(command.tempPassword()),
                    OrderAccessDuration.from(command.orderAccessDuration()),
                    ModmailTranscriptAccessDuration.from(command.modmailTranscriptAccessDuration()),
                    requestingStaffAccount.getId(),
                    command.permissionsIds().stream().map(PermissionId::from).collect(Collectors.toSet())
            );
            staffAccountWriteRepo.create(staffAccount);

            log.info(
                    "Staff account registered: id={}, username={}, status={}, registeredBy={}",
                    staffAccount.getId().getValue(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.getStatus(),
                    staffAccount.getRegisteredBy().getValue()
            );

            return new RegisterStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Staff account registration failed for requestingStaffId={}: domain rule violation - {}",
                    command.id(), ex.getMessage());
            throw appExceptionHandler.handleDomainException(ex);
        }
    }
}
