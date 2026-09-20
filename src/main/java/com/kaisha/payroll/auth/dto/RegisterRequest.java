package com.kaisha.payroll.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String email;
    private String companyId;
    private String password;
    private String confirmPassword;
    private String companyName;
    private String location;
    private String telephoneNumber;
}