
package com.kaisha.payroll.auth.dto;

public record CreateStaffLoginRequest(

        String employeeId,

        String username,

        String email,

        String mobileNumber,

        String password,

        String confirmPassword

) {}
