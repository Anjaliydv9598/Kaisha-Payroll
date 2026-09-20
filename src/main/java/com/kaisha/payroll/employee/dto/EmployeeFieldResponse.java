package com.kaisha.payroll.employee.dto;

public class EmployeeFieldResponse {

    private Long fieldId;
    private String employeeId;
    private String fieldName;
    private String fieldValue;

    public EmployeeFieldResponse() {
    }

    public EmployeeFieldResponse(
            Long fieldId,
            String employeeId,
            String fieldName,
            String fieldValue
    ) {
        this.fieldId = fieldId;
        this.employeeId = employeeId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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