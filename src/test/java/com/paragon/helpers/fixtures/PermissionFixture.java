package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.PermissionCategory;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.PermissionId;

import java.util.UUID;

public class PermissionFixture {
    private String id = UUID.randomUUID().toString();
    private String code = "MANAGE_ACCOUNTS";
    private PermissionCategory category = PermissionCategory.ACCOUNTS;
    private String description = "Permission to manage accounts";

    public PermissionFixture withId(String value) {
        this.id = value;
        return this;
    }

    public PermissionFixture withCode(String value) {
        this.code = value;
        return this;
    }

    public PermissionFixture withCategory(PermissionCategory value) {
        this.category = value;
        return this;
    }

    public PermissionFixture withDescription(String value) {
        this.description = value;
        return this;
    }

    public Permission build() {
        return Permission.createFrom(
                PermissionId.from(id),
                PermissionCode.of(code),
                category,
                description
        );
    }

    public static Permission validPermission() {
        return new PermissionFixture().build();
    }
}
