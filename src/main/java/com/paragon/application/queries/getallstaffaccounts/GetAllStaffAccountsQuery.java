package com.paragon.application.queries.getallstaffaccounts;

public record GetAllStaffAccountsQuery(
        String status,
        String enabledBy,
        String disabledBy,
        String createdBefore,
        String createdAfter
) {
}
