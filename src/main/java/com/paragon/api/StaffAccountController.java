package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("v1/staff-accounts")
public class StaffAccountController {

//    @PostMapping
//    public ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>> register(@RequestBody RegisterStaffAccountRequestDto requestDto) {
//
//    }
}
