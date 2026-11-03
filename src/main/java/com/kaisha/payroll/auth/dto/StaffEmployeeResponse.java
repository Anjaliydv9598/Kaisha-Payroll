
package com.kaisha.payroll.auth.dto;

public record StaffEmployeeResponse(
        Long id,
        String employeeId,
        boolean hasLogin
) {}
