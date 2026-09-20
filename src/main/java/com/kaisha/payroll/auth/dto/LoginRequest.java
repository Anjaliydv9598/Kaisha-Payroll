package com.kaisha.payroll.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String identifier;
    private String password;

}