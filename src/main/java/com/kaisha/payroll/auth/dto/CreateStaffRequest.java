package com.kaisha.payroll.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateStaffRequest {

    private String employeeId;

    private String username;

    private String email;

    private String mobileNumber;

    private String password;

    private String confirmPassword;
}