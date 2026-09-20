package com.kaisha.payroll.salary.dto;

import java.util.ArrayList;
import java.util.List;

public class SalaryRequest {

    private String employeeId;

    private String payPeriod;

    private List<SalaryComponentDto>
            components = new ArrayList<>();

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            String employeeId) {

        this.employeeId =
                employeeId;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(
            String payPeriod) {

        this.payPeriod =
                payPeriod;
    }

    public List<SalaryComponentDto>
    getComponents() {

        return components;
    }

    public void setComponents(
            List<SalaryComponentDto> components) {

        this.components =
                components;
    }
}