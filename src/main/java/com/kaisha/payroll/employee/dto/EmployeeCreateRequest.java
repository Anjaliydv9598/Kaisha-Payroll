package com.kaisha.payroll.employee.dto;

public class EmployeeCreateRequest {

    private String employeeId;

    public EmployeeCreateRequest() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}