package com.kaisha.payroll.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String resetToken;
    private String newPassword;
    private String confirmPassword;
}