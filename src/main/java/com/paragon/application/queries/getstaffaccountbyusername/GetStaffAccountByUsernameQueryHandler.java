package com.paragon.application.queries.getstaffaccountbyusername;

import com.paragon.application.queries.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetStaffAccountByUsernameQueryHandler implements QueryHandler<GetStaffAccountByUsernameQuery, GetStaffAccountByUsernameQueryResponse> {
    @Override
    public GetStaffAccountByUsernameQueryResponse handle(GetStaffAccountByUsernameQuery query) {
        return null;
    }
}
