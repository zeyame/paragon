package com.paragon.domain.models.entities;

import com.paragon.domain.enums.PermissionCategory;
import com.paragon.domain.exceptions.entity.PermissionException;
import com.paragon.domain.exceptions.entity.PermissionExceptionInfo;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.PermissionId;
import lombok.Getter;

@Getter
public class Permission extends Entity<PermissionId> {

    private final PermissionCode code;
    private final PermissionCategory category;
    private String description;

    private Permission(PermissionId id, PermissionCode code, PermissionCategory category, String description) {
        super(id);
        this.code = code;
        this.category = category;
        this.description = description;
    }

    public static Permission create(PermissionCode code, PermissionCategory category, String description) {
        assertValidPermission(code, category);
        return new Permission(PermissionId.generate(), code, category, description);
    }

    private static void assertValidPermission(PermissionCode code, PermissionCategory category) {
        if (code == null) {
            throw new PermissionException(PermissionExceptionInfo.codeRequired());
        }
        if (category == null) {
            throw new PermissionException(PermissionExceptionInfo.categoryRequired());
        }
    }
}
