package com.paragon.application.queries.getstaffaccountbyusername;

import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;

import java.util.Optional;

public record GetStaffAccountByUsernameQueryResponse(Optional<StaffAccountSummary> staffAccountSummary) {
}
