package com.kaisha.payroll.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffEmployeeResponse {

    private Long id;

    private String employeeId;

    private boolean hasLogin;
}