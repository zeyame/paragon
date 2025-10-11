package com.paragon.domain.models.constants;

import com.paragon.domain.models.valueobjects.PermissionCode;

public final class SystemPermissions {

    private SystemPermissions() {}

    public static final PermissionCode VIEW_ACCOUNTS_LIST = PermissionCode.of("VIEW_ACCOUNTS_LIST");
    public static final PermissionCode MANAGE_ACCOUNTS = PermissionCode.of("MANAGE_ACCOUNTS");
    public static final PermissionCode RESET_ACCOUNT_PASSWORD = PermissionCode.of("RESET_ACCOUNT_PASSWORD");
    public static final PermissionCode APPROVE_PASSWORD_CHANGE = PermissionCode.of("APPROVE_PASSWORD_CHANGE");
    public static final PermissionCode APPROVE_ORDER_ACCESS = PermissionCode.of("APPROVE_ORDER_ACCESS");
    public static final PermissionCode APPROVE_MODMAIL_TRANSCRIPT_ACCESS = PermissionCode.of("APPROVE_MODMAIL_TRANSCRIPT_ACCESS");
    public static final PermissionCode VIEW_LOGIN_LOGS = PermissionCode.of("VIEW_LOGIN_LOGS");
    public static final PermissionCode VIEW_PASSWORD_CHANGE_LOGS = PermissionCode.of("VIEW_PASSWORD_CHANGE_LOGS");
    public static final PermissionCode VIEW_MODMAIL_LOGS = PermissionCode.of("VIEW_MODMAIL_LOGS");
    public static final PermissionCode VIEW_WOOCOMMERCE_LOGS = PermissionCode.of("VIEW_WOOCOMMERCE_LOGS");
    public static final PermissionCode VIEW_BACKUP_LIST = PermissionCode.of("VIEW_BACKUP_LIST");
    public static final PermissionCode ACCESS_BACKUP_CONTENT = PermissionCode.of("ACCESS_BACKUP_CONTENT");
    public static final PermissionCode LOAD_BACKUP_CONTENT = PermissionCode.of("LOAD_BACKUP_CONTENT");
    public static final PermissionCode VIEW_SYNCED_MEMBERS = PermissionCode.of("VIEW_SYNCED_MEMBERS");
    public static final PermissionCode MIGRATE_SYNCED_MEMBERS = PermissionCode.of("MIGRATE_SYNCED_MEMBERS");
    public static final PermissionCode VIEW_MODMAIL_TRANSCRIPT_LIST = PermissionCode.of("VIEW_MODMAIL_TRANSCRIPT_LIST");
    public static final PermissionCode CREATE_MODMAIL_TRANSCRIPT_PUBLIC_SHARE_LINK = PermissionCode.of("CREATE_MODMAIL_TRANSCRIPT_PUBLIC_SHARE_LINK");
    public static final PermissionCode VIEW_ORDER_LIST = PermissionCode.of("VIEW_ORDER_LIST");
    public static final PermissionCode REQUEST_CENSORED_ORDER_CONTENT = PermissionCode.of("REQUEST_CENSORED_ORDER_CONTENT");
    public static final PermissionCode INSTANT_ACCESS_CENSORED_ORDER_CONTENT = PermissionCode.of("INSTANT_ACCESS_CENSORED_ORDER_CONTENT");
    public static final PermissionCode CREATE_ORDER_PUBLIC_SHARE_LINK = PermissionCode.of("CREATE_ORDER_PUBLIC_SHARE_LINK");
    public static final PermissionCode VIEW_PRODUCT_LIST = PermissionCode.of("VIEW_PRODUCT_LIST");
    public static final PermissionCode VIEW_PRODUCT_STOCK_COUNT = PermissionCode.of("VIEW_PRODUCT_STOCK_COUNT");
    public static final PermissionCode VIEW_PRODUCT_SALES_COUNT = PermissionCode.of("VIEW_PRODUCT_SALES_COUNT");
    public static final PermissionCode VIEW_EARLY_DISPUTE_WARNINGS = PermissionCode.of("VIEW_EARLY_DISPUTE_WARNINGS");
}
