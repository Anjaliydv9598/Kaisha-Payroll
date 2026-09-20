package com.kaisha.payroll.employee.dto;

import com.kaisha.payroll.employee.entity.EmployeeChangeRequestType;

public class EmployeeChangeRequestDto {

    private EmployeeChangeRequestType requestType;

    private Long fieldId;

    private String fieldName;

    private String fieldValue;

    private String reason;

    public EmployeeChangeRequestDto() {
    }

    public EmployeeChangeRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(EmployeeChangeRequestType requestType) {
        this.requestType = requestType;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}