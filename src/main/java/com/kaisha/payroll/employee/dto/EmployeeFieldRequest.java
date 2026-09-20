package com.kaisha.payroll.employee.dto;

public class EmployeeFieldRequest {

    private String fieldName;
    private String fieldValue;

    public EmployeeFieldRequest() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}