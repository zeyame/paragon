package com.paragon.application.queries.getallstaffaccounts;

import java.util.List;

public record GetAllStaffAccountsQueryResponse(List<StaffAccountSummary> staffAccountSummaries) {}
